package com.gxl.Lighting.consumer;

import com.gxl.Lighting.consumer.processor.NotifyProcessor;
import com.gxl.Lighting.netty.*;
import com.gxl.Lighting.netty.param.NotifyCommandParam;
import com.gxl.Lighting.rpc.DefaultRPCInvocation;
import com.gxl.Lighting.NamedThreadFactory;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.meta.RegisterMeta;
import com.gxl.Lighting.netty.meta.ServiceMeta;
import com.gxl.Lighting.netty.meta.SubscribeMeta;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.enums.SerializationEnum;
import com.gxl.Lighting.proxy.ProxyFactory;
import com.gxl.Lighting.rpc.*;
import com.gxl.Lighting.netty.param.SubscribeCommandParam;
import com.gxl.Lighting.netty.param.UnSubscribeCommandParam;
import com.gxl.Lighting.util.StringUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultConsumer implements Consumer {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultConsumer.class);

    /**
     * 注意 这些接口 上是有 注解信息的  需要通过这个  来 获取
     */
    private List<Class<?>> services = new CopyOnWriteArrayList<Class<?>>();

    /**
     * 维护 服务类的 rpc元数据
     */
    private ConcurrentMap<Class, RPCMeta> rpcMeta = new ConcurrentHashMap<Class, RPCMeta>();

    /**
     * 获取到的 服务提供者信息
     */
    private ConcurrentMap<String, CopyOnWriteArrayList<RegisterMeta>> registerInfo = new ConcurrentHashMap<String, CopyOnWriteArrayList<RegisterMeta>>();

    /**
     * 连接到 注册中心 和  服务提供者的 客户端
     */
    private Client client;

    private String providerAddress;

    private AtomicBoolean start = new AtomicBoolean(false);

    /**
     * 关于 集群调用 发送请求包 处理返回结果的逻辑 全部由这个对象完成
     */
    private RPCInvocation rpcInvocation;

    /**
     * 订阅的元数据
     */
    private SubscribeMeta meta;

    /**
     * 订阅的超时时间
     */
    private long subscribeTimeout;

    private static final long DEFAULT_SUBSCRIBETIMEOUT = 3000;

    /**
     * 这个是 动态代理对象 用户调用后 就会自动发起请求
     */
    private Object service;

    private String[] registryAddresses;

    private String monitorAddress;

    /**
     * 重连到注册中心的 时间间隔
     */
    private long republishInterval = 1000;

    /**
     * 可以, 拼接
     */
    private String serviceName;

    /**
     * 后台订阅到注册中心的定时器
     */
    private Timer timer = new HashedWheelTimer(new NamedThreadFactory("consumerReconnect.thread", true));

    private BlockingQueue<String> failRegistryAddress = new LinkedBlockingQueue<String>();

    private TimerTask task = new TimerTask() {
        public void run(Timeout timeout) throws Exception {
            String registryAddress = failRegistryAddress.take();
            if (start.get()) {
                doSubscributeAysnc(registryAddress);
                timer.newTimeout(this, republishInterval, TimeUnit.MILLISECONDS);
            }
        }
    };

    /**
     * 失败后 继续将任务添加到队列中
     */
    private Callback callback = new Callback() {
        public void callback(ResponseFuture future) {
            if (!future.getResponse().getResult().isSuccess()) {
                if (start.get()) {
                    DefaultConsumer.this.getFailRegistryAddress().add(future.getRemoteAddress());
                }
            }
        }
    };

    private void doSubscributeAysnc(String registryAddress) {
        String[] address = StringUtil.split(registryAddress, ":");
        SubscribeCommandParam param = new SubscribeCommandParam();
        param.setServiceName(serviceName);
        param.setMeta(meta);
        Request request = Request.createRequest(RequestEnum.SUBSCRIBE, param);
        request.setInvokeType(InvokeTypeEnum.ASYNC.getInvokeType());
        request.setSerialization(SerializationEnum.defaultSerialization());
        client.invokeAsync(registryAddress, request, callback, subscribeTimeout);
    }


    public synchronized void notify(List<RegisterMeta> services) {
        //这里获得的是全量数据 然后需要按照服务名 进行分类  每个ServiceMeta 对应一个 接口
        //清空旧数据
        registerInfo.clear();
        for (RegisterMeta meta : services) {
            for (ServiceMeta serviceMeta : meta.getServiceMeta()) {
                if (registerInfo.get(serviceMeta.getServiceName()) == null) {
                    registerInfo.putIfAbsent(serviceMeta.getServiceName(), new CopyOnWriteArrayList<RegisterMeta>());
                }
                List<RegisterMeta> list = registerInfo.get(serviceMeta.getServiceName());
                list.add(meta);
            }
        }
    }

    public DefaultConsumer() {
        this(DEFAULT_SUBSCRIBETIMEOUT);
    }

    public DefaultConsumer(long subscribeTimeout) {
        this.subscribeTimeout = subscribeTimeout;
        client = new DefaultClient();
        client.getProcessorManager().registerProcessor(RequestEnum.NOTIFY, new NotifyProcessor(this));
        timer.newTimeout(task, republishInterval, TimeUnit.MILLISECONDS);
    }


    public void reset() {
        services.clear();
        rpcMeta.clear();
        registerInfo.clear();
    }

    public void subscribe() {
        if (start.compareAndSet(false, true)) {
            if (checkParam(services)) {
                getRPCInfo();
                meta = SubscribeMeta.newMeta(services);
                serviceName = serviceName();

                //开始向注册中心订阅服务
                for (String registryAddress : registryAddresses) {
                    boolean registerResult = doSubscribute(registryAddress);
                    if (registerResult) {
                        logger.warn("在地址为{}的注册中心成功订阅服务", registryAddress);
                    } else {
                        logger.warn("到注册中心订阅服务失败, 在后台开启重连任务");
                        failRegistryAddress.add(registryAddress);
                    }
                }
            } else {
                start.set(false);
            }
        } else {
            logger.info("消费者已启动请不要重复订阅");
        }
    }

    private Class<?>[] getArray(List<Class<?>> services) {
        Class<?>[] clazz = new Class<?>[services.size()];
        for (int i = 0; i < services.size(); i++) {
            clazz[i] = services.get(i);
        }
        return clazz;
    }

    public void shutdownGracefully() {
        if (start.compareAndSet(true, false)) {
            //关闭前先取消之前订阅的服务
            for (String registryAddress : registryAddresses) {
                boolean result = doUnSubscribe(registryAddress);
                if (result) {
                    logger.warn("服务注销成功");
                } else {
                    logger.warn("注销失败放弃注销发布地址为{}的{}服务", registryAddress, serviceName);
                }
            }
            timer.stop();
            client.shutdownGracefully();
        } else {
            logger.warn("该服务提供者正在关闭");
        }
    }

    /**
     * 取消订阅
     *
     * @param registryAddress
     * @return
     */
    private boolean doUnSubscribe(String registryAddress) {
        UnSubscribeCommandParam param = new UnSubscribeCommandParam();
        param.setMeta(meta);
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.UNSUBSCRIBE, param);
        request.setInvokeType(InvokeTypeEnum.SYNC.getInvokeType());
        request.setSerialization(SerializationEnum.defaultSerialization());
        try {
            Response response = client.invokeSync(registryAddress, request, subscribeTimeout);
            return response.getResult().isSuccess();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingSendException e) {
            e.printStackTrace();
        } catch (RemotingTimeoutException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean doSubscribute(String registryAddress) {
        String[] address = StringUtil.split(registryAddress, ":");
        SubscribeCommandParam param = new SubscribeCommandParam();
        param.setServiceName(serviceName);
        param.setMeta(meta);
        Request request = Request.createRequest(RequestEnum.SUBSCRIBE, param);
        request.setInvokeType(InvokeTypeEnum.SYNC.getInvokeType());
        request.setSerialization(SerializationEnum.defaultSerialization());
        try {
            Response response = client.invokeSync(registryAddress, request, subscribeTimeout);
            return response.getResult().isSuccess();
//            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingSendException e) {
            e.printStackTrace();
        } catch (RemotingTimeoutException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String serviceName() {
        Iterator<Class<?>> iterator = services.iterator();
        List<String> list = new ArrayList<String>();
        while (iterator.hasNext()) {
            list.add(iterator.next().getSimpleName());
        }
        return StringUtil.join(list.toArray(new String[0]), ",");
    }

    /**
     * 从 注解信息中解析参数  Class 好像不用重写equeal 同一类型的class判定是相等的
     */
    private void getRPCInfo() {
        for (Class clazz : services) {
            RPC annotation = (RPC) clazz.getAnnotation(RPC.class);
            checkRPCAnnotationValid(annotation);
            RPCMeta meta = new RPCMeta();
            meta.setLoadBalance(annotation.balanceStrategy());
            meta.setSerialization(annotation.serialization());
            meta.setTarget(clazz);
            meta.setServiceName(clazz.getSimpleName());
            meta.setTimeout(annotation.timeout());
            meta.setInvokeType(annotation.invokeType());
            meta.setVip(annotation.vip());
            rpcMeta.put(clazz, meta);
        }
    }

    /**
     * 校验参数合法性
     *
     * @param annotation
     */
    private void checkRPCAnnotationValid(RPC annotation) {
        if (!SerializationEnum.find(annotation.serialization())) {
            throw new IllegalArgumentException("RPC注解上设置了异常的序列化方式");
        }
        if (!InvokeTypeEnum.find(annotation.invokeType())) {
            throw new IllegalArgumentException("RPC注解上设置了异常的通信方式");
        }
    }

    /**
     * 检验需要 订阅的 接口信息是否正常
     *
     * @param services
     */
    private boolean checkParam(List<Class<?>> services) {
        if (services.size() == 0) {
            logger.warn("订阅的服务列表不能为空");
            return false;
        }
        for (Class<?> clazz : services) {
            if (!clazz.isInterface()) {
                logger.warn(clazz.getClass().getSimpleName() + "不是接口 不能作为服务");
                return false;
            }
            if (!clazz.isAnnotationPresent(RPC.class)) {
                logger.warn(clazz.getClass().getSimpleName() + "没有包含RPC注解");
                return false;
            }
        }
        if (StringUtil.isEmpty(monitorAddress)) {
            logger.warn("监控中心地址未设置 不能启动");
            return false;
        }
        if (registryAddresses.length == 0 || registryAddresses == null) {
            logger.warn("注册中心地址未设置 不能启动");
            return false;
        }
        return true;
    }

    public String[] getRegistryAddresses() {
        return registryAddresses;
    }

    public void setRegistryAddresses(String[] address) {
        this.registryAddresses = address;
    }

    public String getMonitorAddress() {
        return monitorAddress;
    }

    public void setMonitorAddress(String address) {
        this.monitorAddress = address;
    }

    public void addSubscribeService(Class<?> o) {
        if (start.get()) {
            logger.warn("消费者正在启动中 请先关闭再注册新服务");
            return;
        }
        this.services.add(o);
    }

    public void addSubscribeServices(Class<?>... o) {
        if (start.get()) {
            logger.warn("消费者正在启动中 请先关闭再注册新服务");
            return;
        }
        for (Class<?> clazz : o) {
            services.add(clazz);
        }
    }

    public void removeSubscribeService(Class<?> o) {
        if (start.get()) {
            logger.warn("消费者正在启动中 请先关闭");
            return;
        }
        this.services.remove(o);
    }

    public void removeSuscribeServices(Class<?>... o) {
        if (start.get()) {
            logger.warn("消费者正在启动中 请先关闭");
            return;
        }
        for (Class<?> clazz : o) {
            services.remove(clazz);
        }
    }

    /**
     * 直连服务提供者
     *
     * @param providerAddress
     */
    @Override
    public void directConnection(String providerAddress) {
        this.providerAddress = providerAddress;
        if (start.compareAndSet(false, true)) {
            if (checkProvider()) {

            }
        }
    }

    private boolean checkProvider() {
        if (!StringUtil.isEmpty(providerAddress)) {
            return true;
        }
        return false;
    }

    public boolean unSubscribe() {
        return false;
    }

    public Object getService() {
        if (service == null) {
            Class<?>[] interfaces = getArray(services);
            service = ProxyFactory.getProxy(new DefaultRPCInvocation(this), interfaces);
        }
        return service;
    }

    public void setService(Object o) {
        this.service = o;
    }

    public RPCMeta getAnnotationInfo(Class<?> service) {
        return rpcMeta.get(service);
    }

    public Class<?>[] getServices() {
        return services.toArray(new Class<?>[0]);
    }

    public List<RegisterMeta> getRegisterInfo(String serviceName) {
        return registerInfo.get(serviceName);
    }

    public Client getClient() {
        return client;
    }

    public BlockingQueue<String> getFailRegistryAddress() {
        return failRegistryAddress;
    }

    public void setFailRegistryAddress(BlockingQueue<String> failRegistryAddress) {
        this.failRegistryAddress = failRegistryAddress;
    }

    public boolean isShutdown() {
        return !start.get();
    }
}
