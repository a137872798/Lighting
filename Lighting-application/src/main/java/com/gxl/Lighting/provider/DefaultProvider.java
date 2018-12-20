package com.gxl.Lighting.provider;

import com.gxl.Lighting.ConcurrentHashSet;
import com.gxl.Lighting.NamedThreadFactory;
import com.gxl.Lighting.Version;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.Client;
import com.gxl.Lighting.netty.DefaultClient;
import com.gxl.Lighting.netty.DefaultServer;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.provider.processor.InvokerProcessor;
import com.gxl.Lighting.proxy.Invoker;
import com.gxl.Lighting.rpc.*;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.UnRegisterCommandParam;
import com.gxl.Lighting.util.AddressUtil;
import com.gxl.Lighting.util.StringUtil;
import com.sun.media.jfxmedia.logging.Logger;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import sun.rmi.runtime.Log;

import java.net.InetSocketAddress;
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

    //TODO 这里要思考 怎么才能指定端口
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
     * 注册的 超时时间
     */
    private long registerTimeout;

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
    private ConcurrentHashSet<Object> exports = new ConcurrentHashSet<Object>();

    private final AtomicBoolean start = new AtomicBoolean(false);

    /**
     * 该服务提供者 提供的 服务  , 分割
     */
    private String serviceName;

    private final TimerTask republishTask = new TimerTask() {
        public void run(Timeout timeout) throws Exception {
            String registryAddress = getFailRegistryAddress().take();
            doPublishAsync(registryAddress);
            timer.newTimeout(this, republishInterval, TimeUnit.MILLISECONDS);
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
        param.setVersion(Version.version());
        param.setAddress(AddressUtil.socketAddressToAddress((InetSocketAddress) client.channelTable().get(registryAddress).localAddress()));
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.REGISTRY, param);
        Response response = null;
        client.invokeAsync(registryAddress, request, republishCallback, registerTimeout);
    }

    public DefaultProvider(long registerTimeout) {
        this.registerTimeout = registerTimeout;
        init();
    }

    private void init() {
        client = new DefaultClient();
        server = new DefaultServer();
        vipServer = new DefaultServer();
        server.getProcessorManager().registerProcessor(RequestEnum.INVOKE, new InvokerProcessor());
        vipServer.getProcessorManager().registerProcessor(RequestEnum.INVOKE, new InvokerProcessor());
        timer = new HashedWheelTimer(new NamedThreadFactory("providerRepublish", true), republishInterval, TimeUnit.MILLISECONDS);
        timer.newTimeout(republishTask, republishInterval, TimeUnit.MILLISECONDS);
    }

    //每次 发布 必须是 提供者 处于停止 状态  可以做成热部署 但是在并发情况下会有很多发布请求吧 这样不知道是否合适

    public void addPublishService(Object o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者再发布新服务");
            return;
        }
        exports.add(o);
    }

    public void addPublishServices(Object... o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者再发布新服务");
            return;
        }
        exports.addAll(o);
    }


    public void removePublishService(Object o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者");
            return;
        }
        exports.remove(o);
    }

    public void removePublishServices(Object... o) {
        if (start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者");
            return;
        }
        exports.removeAll(o);
    }

    /**
     * 发布
     */
    public void publish() {
        if (start.compareAndSet(false, true)) {
            if (checkParam(monitorAddress, registryAddresses, exports)) {
                invoker = createInvoker();
                server.start();
                vipServer.start();
                ArrayList<String> serviceNames = new ArrayList<String>();
                for (Object o : exports) {
                    serviceNames.add(o.getClass().getSuperclass().getSimpleName());
                }

                logger.info("正在发布服务...");
                serviceName = StringUtil.join(serviceNames.toArray(new String[0]), ",");
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
            logger.warn("该服务提供者已经启动");
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
            logger.info("正在注销发布的服务...");
            for (String registryAddress : registryAddresses) {

                //注销 不采用 后台线程执行的方式
                boolean result = unPublish(registryAddress, serviceName);
                if (result) {
                    logger.warn("服务注销成功");
                } else {
                    logger.warn("放弃注销发布地址为{}的{}服务", registryAddress, serviceName);
                }
            }
            client.shutdownGracefully();
            server.shutdownGracefully();
            vipServer.shutdownGracefully();
            timer.stop();
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
        param.setVersion(Version.version());
        param.setAddress(AddressUtil.socketAddressToAddress((InetSocketAddress) client.channelTable().get(registryAddress).localAddress()));
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.REGISTRY, param);
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
    public boolean unPublish(String registryAddress, String serviceName) {

        UnRegisterCommandParam param = new UnRegisterCommandParam();
        param.setVersion(Version.version());
        param.setServiceName(serviceName);
        param.setAddress(AddressUtil.socketAddressToAddress((InetSocketAddress) client.channelTable().get(registryAddress).localAddress()));
        Request request = Request.createRequest(RequestEnum.UNREGISTRY, param);
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

    private Invoker createInvoker() {
        return new Invoker(exports.toList());
    }

    /**
     * 启动服务前 检测 服务是否允许正常启动
     *
     * @param monitorAddress
     * @param registryAddresses
     * @param exports
     */
    private boolean checkParam(String monitorAddress, String[] registryAddresses, ConcurrentHashSet<Object> exports) {
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

    public ConcurrentHashSet<Object> getExports() {
        return exports;
    }

    public void setExports(ConcurrentHashSet<Object> exports) {
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
