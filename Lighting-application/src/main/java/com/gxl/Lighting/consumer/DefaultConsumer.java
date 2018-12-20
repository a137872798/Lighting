package com.gxl.Lighting.consumer;

import com.gxl.Lighting.ConcurrentHashSet;
import com.gxl.Lighting.NotifyListener;
import com.gxl.Lighting.Version;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.meta.ServiceMeta;
import com.gxl.Lighting.meta.SubscributeMeta;
import com.gxl.Lighting.netty.Client;
import com.gxl.Lighting.netty.DefaultClient;
import com.gxl.Lighting.proxy.ProxyFactory;
import com.gxl.Lighting.rpc.*;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.SubscribeCommandParam;
import com.gxl.Lighting.util.AddressUtil;
import com.gxl.Lighting.util.StringUtil;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultConsumer implements Consumer{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultConsumer.class);

    /**
     * 注意 这些接口 上是有 注解信息的  需要通过这个  来 获取
     */
    private ConcurrentHashSet<Class<?>> services = new ConcurrentHashSet<Class<?>>();

    /**
     * 维护 服务类的 rpc元数据
     */
    private ConcurrentMap<Class, RPCMeta> rpcMeta = new ConcurrentHashMap<Class, RPCMeta>();

    /**
     * 获取到的 服务提供者信息
     */
    private ConcurrentMap<String, CopyOnWriteArrayList<RegisterMeta>> registerInfo =
            new ConcurrentHashMap<String, CopyOnWriteArrayList<RegisterMeta>>();

    /**
     * 连接到 注册中心 和  服务提供者的 客户端
     */
    private Client client;

    private AtomicBoolean start = new AtomicBoolean(false);

    private ProxyFactory proxyFactory = new ProxyFactory();

    /**
     * 关于 集群调用 发送请求包 处理返回结果的逻辑 全部由这个对象完成
     */
    private RPCInvocation rpcInvocation;

    /**
     * 订阅的超时时间
     */
    private long subscribeTimeout;

    /**
     * 这个对象应该是 完成 订阅后 就设置的 然后 用户直接拿这个对象就好
     */
    private Object service;

    private String[] registryAddresses;

    private String monitorAddress;

    private NotifyListener DEFAULT_NOTIFYLISTENER = new NotifyListener() {
        public void notify(List<RegisterMeta> services) {
            //这里获得的是全量数据 然后需要按照服务名 进行分类
            for(RegisterMeta meta : services){
                for(ServiceMeta serviceMeta : meta.getServiceMeta()){
                    if(registerInfo.get(serviceMeta.getServiceName()) == null) {

                        CopyOnWriteArrayList list = new CopyOnWriteArrayList();
                        CopyOnWriteArrayList old = registerInfo.putIfAbsent(serviceMeta.getServiceName(), list);
                        if(old != null){
                            list = old;
                        }

                    } else {

                    }
                }
            }
        }
    };

    public DefaultConsumer(long subscribeTimeout){
        this.subscribeTimeout = subscribeTimeout;
        init();
    }

    private void init() {
        client = new DefaultClient();
    }

    public void reset() {
        services.clear();
        rpcMeta.clear();
        registerInfo.clear();
    }

    public void subscribe(NotifyListener listener) {
        if (start.compareAndSet(false, true)) {
            if (checkParam(services)){
                getRPCInfo();
                client.start();

                String serviceName = serviceName();
                //开始向注册中心订阅服务
                boolean atLeastSuccess = false;
                for (String registryAddress : registryAddresses) {
                    boolean registerResult = doSubscribute(registryAddress, serviceName, listener);
                    int times = 0;
                    if (registerResult) {
                        atLeastSuccess = true;
                    }
                    if (!registerResult && times <= 3) {
                        logger.warn("到注册中心订阅服务失败, 正在重试");
                        times++;
                        registerResult = doSubscribute(registryAddress, serviceName, listener);
                    }
                    if (registerResult) {
                        logger.warn("在地址为{}的注册中心成功订阅服务", registryAddress);
                    } else {
                        logger.warn("在地址为{}的注册中心订阅服务失败", registryAddresses);
                    }
                }
                if (!atLeastSuccess) {
                    logger.warn("该服务没有在任意一台注册中心成功订阅服务 现在关闭客户端请稍后重启");
                    shutdownGracefully();
                }
            }
        }
    }

    public void shutdownGracefully() {
        if (start.compareAndSet(true, false)) {
            //关闭前先取消之前发布的服务
            ArrayList<String> serviceNames = new ArrayList<String>();
            for (Object o : exports) {
                serviceNames.add(o.getClass().getSuperclass().getSimpleName());
            }
            String serviceName = StringUtil.join(serviceNames.toArray(new String[0]), ",");
            for (String registryAddress : registryAddresses) {
                boolean result = unPublish(registryAddress, serviceName);
                int times = 0;
                if (!result && times <= 3) {
                    logger.warn("注销发布失败 正在尝试重新注销");
                    times++;
                    result = unPublish(registryAddress, serviceName);
                }
                if (result) {
                    logger.warn("服务注销成功");
                } else {
                    logger.warn("放弃注销发布地址为{}的{}服务", registryAddress, serviceName);
                }
            }
            client.shutdownGracefully();
            server.shutdownGracefully();
            vipServer.shutdownGracefully();
        } else {
            logger.warn("该服务提供者正在关闭");
        }
    }

    private boolean doSubscribute(String registryAddress, String serviceName, NotifyListener listener){
        String[] address = StringUtil.split(registryAddress, ":");
        client.connect(address[0], Integer.valueOf(address[1]));
        SubscribeCommandParam param = new SubscribeCommandParam();
        param.setAddress(AddressUtil.socketAddressToAddress((InetSocketAddress) client.channelTable()
                .get(registryAddress).localAddress()));
        param.setServiceName(serviceName);
        param.setVersion(Version.version());
        param.setListener(listener);
        Request request = Request.createRequest(RequestEnum.SUBSCRIBE, param);
        try {
            Response response = client.invokeSync(registryAddress, request, subscribeTimeout);
            return response.isSuccess();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (RemotingSendException e) {
            e.printStackTrace();
        } catch (RemotingTimeoutException e) {
            e.printStackTrace();
        }

        return false;
    }

    private String serviceName(){
        Iterator<Class<?>> iterator = services.iterator();
        List<String> list = new ArrayList<String>();
        while(iterator.hasNext()){
            list.add(iterator.next().getSimpleName());
        }
        return StringUtil.join(list.toArray(new String[0]), ",");
    }

    /**
     * 从 注解信息中解析参数
     */
    private void getRPCInfo() {
        for(Class clazz : services){
            RPC annotation = (RPC)clazz.getAnnotation(RPC.class);
            RPCMeta meta = new RPCMeta();
            meta.setBalance(annotation.balanceStrategy());
            meta.setSerialization(annotation.serialization());
            meta.setTarget(clazz);
            meta.setServiceName(clazz.getSimpleName());
            meta.setTimeout(annotation.timeout());
            meta.setType(annotation.type());
            meta.setVip(annotation.vip());
            rpcMeta.put(clazz, meta);
        }
    }

    /**
     * 检验需要 订阅的 接口信息是否正常
     * @param services
     */
    private boolean checkParam(ConcurrentHashSet<Class<?>> services) {
        if(services.size() == 0){
            logger.warn("订阅的服务列表不能为空");
            return false;
        }
        for(Class<?> clazz : services){
            if(!clazz.getClass().isInterface()){
                logger.warn(clazz.getClass().getSimpleName() + "不是接口 不能作为服务");
                return false;
            }
            if(!clazz.isAnnotationPresent(RPC.class)) {
                logger.warn(clazz.getClass().getSimpleName() + "没有包含RPC注解");
                return false;
            }
        }
        if(StringUtil.isEmpty(monitorAddress)){
            logger.warn("监控中心地址未设置 不能启动");
            return false;
        }
        if(registryAddresses.length == 0 || registryAddresses == null){
            logger.warn("注册中心地址未设置 不能启动");
            return false;
        }
        return true;
    }

    public String[] getRegistryAddresses() {
        return new String[0];
    }

    public void setRegistryAddresses(String[] address) {
    }

    public String getMonitorAddress() {
        return null;
    }

    public void setMonitorAddress(String address) {

    }

    public void addSubscribeService(Class<?> service) {
        if(start.get()){
            logger.warn("消费者正在启动中 请先关闭再注册新服务");
            return;
        }
        this.services.add(service);
    }

    public void addSubscribeServices(Class<?>... services) {
        if(start.get()){
            logger.warn("消费者正在启动中 请先关闭再注册新服务");
            return;
        }
        this.services.addAll(services);
    }

    public void removeSubscribeService(Class<?> service) {
        if(start.get()){
            logger.warn("消费者正在启动中 请先关闭");
            return;
        }
        this.services.remove(service);
    }

    public void removeSuscribeServices(Class<?>... services) {
        if(start.get()){
            logger.warn("消费者正在启动中 请先关闭");
            return;
        }
        this.services.removeAll(services);
    }

    public boolean unSubscribe() {
        return false;
    }

    public Object getService() {
        return service;
    }


}
