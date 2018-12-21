package com.gxl.Lighting.provider;

import com.gxl.Lighting.ConcurrentHashSet;
import com.gxl.Lighting.NamedThreadFactory;
import com.gxl.Lighting.Version;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.netty.Client;
import com.gxl.Lighting.netty.DefaultClient;
import com.gxl.Lighting.netty.DefaultServer;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.enums.SerializationEnum;
import com.gxl.Lighting.provider.processor.InvokerProcessor;
import com.gxl.Lighting.proxy.Invoker;
import com.gxl.Lighting.rpc.*;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.UnRegisterCommandParam;
import com.gxl.Lighting.util.AddressUtil;
import com.gxl.Lighting.util.StringUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultProvider implements Provider {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProvider.class);

    /**
     * 提供者 作为 注册中心的客户端
     */
    private Client client;

    private static final int DEFAULT_PORT = 8080;

    private static final int VIP_PORT = 8082;

    private Server server;

    private Server vipServer;

    private String monitorAddress;

    private String[] registryAddresses;

    /**
     * 存放注册失败的 地址
     */
    private BlockingQueue<String> failRegistryAddress = new LinkedBlockingQueue<String>();

    private Timer timer;

    /**
     * 注册的元数据
     */
    private RegisterMeta meta;

    /**
     * 注册的 超时时间
     */
    private long registerTimeout;

    private static final long DEFAULT_REGISTERTIMEOUT = 1000;
    /**
     * 重连到注册中心的 时间间隔
     */
    private long republishInterval = 1000;

    /**
     * 封装了调用服务实现类的逻辑
     */
    private Invoker invoker;

    /**
     * 该服务提供者 暴露的服务
     */
    private List<Class<?>> exports = new CopyOnWriteArrayList<Class<?>>();

    private final AtomicBoolean start = new AtomicBoolean(false);

    /**
     * 该服务提供者 提供的 服务  , 分割
     */
    private String serviceName;

    private final TimerTask republishTask = new TimerTask() {
        public void run(Timeout timeout) throws Exception {
        String registryAddress = getFailRegistryAddress().take();
        if(start.get()) {
            doPublishAsync(registryAddress);
            timer.newTimeout(this, republishInterval, TimeUnit.MILLISECONDS);
        }
        }
    };

    private final Callback republishCallback = new Callback() {
        public void callback(ResponseFuture future) {
        //当失败时  重新将任务 添加到 队列中 这样定时器还能继续触发
        if (!future.getResponse().isSuccess()) {
            DefaultProvider.this.getFailRegistryAddress().add(future.getRemoteAddress());
        }
        }
    };

    /**
     * 异步发布任务
     *
     * @param registryAddress
     */
    private void doPublishAsync(String registryAddress) {
        //连接到注册中心
        RegisterCommandParam param = new RegisterCommandParam();
        param.setRegisterMeta(meta);
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.REGISTRY, param);
        request.setSerialization(SerializationEnum.defaultSerialization());
        request.setInvokeType(InvokeTypeEnum.ASYNC.getInvokeType());
        client.invokeAsync(registryAddress, request, republishCallback, registerTimeout);
    }

    public DefaultProvider(){
        this(DEFAULT_REGISTERTIMEOUT);
    }

    public DefaultProvider(long registerTimeout) {
        this.registerTimeout = registerTimeout;
        client = new DefaultClient();
        server = new DefaultServer(DEFAULT_PORT);
        vipServer = new DefaultServer(VIP_PORT);
        server.getProcessorManager().registerProcessor(RequestEnum.INVOKE, new InvokerProcessor());
        vipServer.getProcessorManager().registerProcessor(RequestEnum.INVOKE, new InvokerProcessor());
        timer = new HashedWheelTimer(new NamedThreadFactory("providerRepublish", true), republishInterval, TimeUnit.MILLISECONDS);
        timer.newTimeout(republishTask, republishInterval, TimeUnit.MILLISECONDS);
    }


    private String createServiceName(List<Class<?>> exports) {
        String[] serviceNames = new String[exports.size()];
        for(int i = 0 ;i < exports.size(); i++){
            serviceNames[i] = exports.get(i).getSimpleName();
        }
        return StringUtil.join(serviceNames, ",");
    }

    //每次 发布 必须是 提供者 处于停止 状态  可以做成热部署 但是在并发情况下会有很多发布请求吧 这样不知道是否合适

    public void addPublishService(Class<?> o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者再发布新服务");
            return;
        }
        exports.add(o);
    }

    public void addPublishServices(Class<?>... o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者再发布新服务");
            return;
        }
        for(Class<?> clazz : o){
            exports.add(clazz);
        }
    }


    public void removePublishService(Class<?> o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者");
            return;
        }
        exports.remove(o);
    }

    public void removePublishServices(Class<?>... o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者");
            return;
        }
        for(Class<?> clazz : o){
            exports.remove(clazz);
        }
    }

    /**
     * 发布
     */
    public void publish() throws UnknownHostException {
        if (start.compareAndSet(false, true)) {
            if (checkParam(monitorAddress, registryAddresses, exports)) {
                invoker = createInvoker();
                server.start();
                vipServer.start();
                String address = StringUtil.join(new String[]{InetAddress.getLocalHost()
                        .getHostAddress(),String.valueOf(DEFAULT_PORT)},":");
                serviceName = createServiceName(exports);

                meta = RegisterMeta.newMeta(exports, address);

                logger.info("正在发布服务...");
                for (String registryAddress : registryAddresses) {
                    boolean registerResult = doPublish(registryAddress);
                    if (registerResult) {
                        logger.warn("在地址为{}的注册中心服务发布成功", registryAddress);
                    } else {
                        logger.warn("在地址为{}的注册中心服务发布失败会在后台自动重连", registryAddresses);
                        failRegistryAddress.add(registryAddress);
                    }
                }
            } else {
                //因为 参数校验失败 要 修改成 未启动状态
                start.set(false);
            }
        } else {
            logger.warn("该服务提供者已经启动请不要重复注册");
        }
    }

    public void shutdownGracefully() {
        if (start.compareAndSet(true, false)) {
            //关闭前先取消之前发布的服务
            logger.info("正在注销发布的服务...");
            for (String registryAddress : registryAddresses) {

                //注销 不采用 后台线程执行的方式
                boolean result = doUnPublish(registryAddress);
                if (result) {
                    logger.warn("地址为{}的服务注销成功", registryAddress);
                } else {
                    logger.warn("注销失败,放弃注销发布地址为{}的{}服务", registryAddress, serviceName);
                }
            }
            timer.stop();
            client.shutdownGracefully();
            server.shutdownGracefully();
            vipServer.shutdownGracefully();
        } else {
            logger.warn("该服务提供者正在关闭");
        }
    }

    public void reset() {
        monitorAddress = null;
        registryAddresses = null;
        exports.clear();
    }


    /**
     * 将 服务注册到注册中心
     */
    private boolean doPublish(String registryAddress) {
        RegisterCommandParam param = new RegisterCommandParam();
        param.setRegisterMeta(meta);
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.REGISTRY, param);
        request.setSerialization(SerializationEnum.defaultSerialization());
        request.setInvokeType(InvokeTypeEnum.SYNC.getInvokeType());
        Response response = null;
        try {
            response = client.invokeSync(registryAddress, request, registerTimeout);
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

    /**
     * 针对之前发布的所有服务 进行注销
     */
    private boolean doUnPublish(String registryAddress) {
        UnRegisterCommandParam param = new UnRegisterCommandParam();
        param.setMeta(meta);
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.UNREGISTRY, param);
        request.setSerialization(SerializationEnum.defaultSerialization());
        request.setInvokeType(InvokeTypeEnum.SYNC.getInvokeType());
        Response response = null;
        try {
            response = client.invokeSync(registryAddress, request, 3);
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

//    public void unPublish(){
//        for(String registryAddress : registryAddresses){
//            doUnPublish(registryAddress);
//        }
//    }

    private Invoker createInvoker() {
        return new Invoker(exports);
    }

    /**
     * 启动服务前 检测 服务是否允许正常启动
     *
     * @param monitorAddress
     * @param registryAddresses
     * @param exports
     */
    private boolean checkParam(String monitorAddress, String[] registryAddresses, List<Class<?>> exports) {
        if (StringUtil.isEmpty(monitorAddress)) {
            logger.info("没有设置监控中心的地址 无法启动");
            return false;
        }
        if (registryAddresses.length == 0 || registryAddresses == null) {
            logger.info("没有设置注册中心的地址 无法启动");
            return false;
        }
        if (exports.isEmpty()) {
            logger.info("该服务提供者没有设置服务 无法启动");
            return false;
        }
        return true;
    }

    public String[] getRegistryAddresses() {
        return registryAddresses;
    }

    public void setRegistryAddresses(String[] addresses) {
        this.registryAddresses = registryAddresses;
    }

    public String getMonitorAddress() {
        return monitorAddress;
    }

    public void setMonitorAddress(String address) {
        this.monitorAddress = monitorAddress;
    }

    public BlockingQueue<String> getFailRegistryAddress() {
        return failRegistryAddress;
    }

    public void setFailRegistryAddress(BlockingQueue<String> failRegistryAddress) {
        this.failRegistryAddress = failRegistryAddress;
    }

    public Timer getTimer() {
        return timer;
    }

    public void setTimer(Timer timer) {
        this.timer = timer;
    }

    public long getRegisterTimeout() {
        return registerTimeout;
    }

    public void setRegisterTimeout(long registerTimeout) {
        this.registerTimeout = registerTimeout;
    }

    public long getRepublishInterval() {
        return republishInterval;
    }

    public void setRepublishInterval(long republishInterval) {
        this.republishInterval = republishInterval;
    }

    public Invoker getInvoker() {
        return invoker;
    }

    public void setInvoker(Invoker invoker) {
        this.invoker = invoker;
    }

    public List<Class<?>> getExports() {
        return exports;
    }

    public void setExports(List<Class<?>> exports) {
        this.exports = exports;
    }

    public AtomicBoolean getStart() {
        return start;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

}
