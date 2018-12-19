package com.gxl.Lighting.provider;

import com.gxl.Lighting.ConcurrentHashSet;
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
import com.gxl.Lighting.util.StringUtil;

import java.util.ArrayList;
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
     * 注册的 超时时间
     */
    private long registerTimeout;

    /**
     * 封装了调用服务实现类的逻辑
     */
    private Invoker invoker;

    /**
     * 该服务提供者 暴露的服务
     */
    private ConcurrentHashSet<Object> exports = new ConcurrentHashSet<Object>();

    private final AtomicBoolean start = new AtomicBoolean(false);

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
    }

    //每次 发布 必须是 提供者 处于停止 状态  可以做成热部署 但是在并发情况下会有很多发布请求吧 这样不知道是否合适

    public void publishService(Object o) {
        if(start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者再发布新服务");
            return;
        }
        exports.add(o);
    }

    public void publishServices(Object... o) {
        if(start.get()) {
            logger.warn("服务提供者已经启动，请先停止提供者再发布新服务");
            return;
        }
        for (Object temp : o) {
            exports.add(o);
        }

    }

    /**
     * 启动服务提供者
     */
    public void start() {
        if (start.compareAndSet(false, true)) {
            if (checkParam(monitorAddress, registryAddresses, exports)) {
                invoker = createInvoker();
                server.start();
                vipServer.start();
                boolean registerResult = register();
                int times = 0;
                if (!registerResult && times <= 3) {
                    logger.warn("发布到注册中心失败, 正在重试");
                    times++;
                    registerResult = register();
                }
                if (registerResult) {
                    logger.warn("服务发布成功");
                } else {
                    logger.warn("服务发布失败,关闭服务提供者");
                    shutdownGracefully();
                }
            }else{
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
            boolean result = unRegister();
            int times = 0;
            if (!result && times <= 3) {
                logger.warn("注销发布失败 正在尝试重新注销");
                times++;
                result = register();
            }
            if (result) {
                logger.warn("服务注销成功");
            } else {
                logger.warn("服务注销失败,关闭服务提供者");
            }
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
    }


    /**
     * 将 服务注册到注册中心
     */
    private boolean register() {
        ArrayList<String> serviceNames = new ArrayList<String>();
        for (Object o : exports) {
            serviceNames.add(o.getClass().getSuperclass().getSimpleName());
        }
        String serviceName = StringUtil.join(serviceNames.toArray(new String[0]), ",");

        for (String registryAddress : registryAddresses) {
            String[] address = StringUtil.split(registryAddress, ":");
            //连接到注册中心
            client.connect(address[0], Integer.valueOf(address[1]));
            RegisterCommandParam param = new RegisterCommandParam();
            param.setVersion(Version.version());
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
        }
        return false;
    }

    /**
     * 针对之前发布的所有服务 进行注销
     */
    public boolean unRegister() {
        ArrayList<String> serviceNames = new ArrayList<String>();
        for (Object o : exports) {
            serviceNames.add(o.getClass().getSuperclass().getSimpleName());
        }
        String serviceName = StringUtil.join(serviceNames.toArray(new String[0]), ",");
        for (String registryAddress : registryAddresses) {
            UnRegisterCommandParam param = new UnRegisterCommandParam();
            param.setVersion(Version.version());
            param.setServiceName(serviceName);
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

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public Server getVipServer() {
        return vipServer;
    }

    public void setVipServer(Server vipServer) {
        this.vipServer = vipServer;
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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public long getRegisterTimeout() {
        return registerTimeout;
    }

    public void setRegisterTimeout(long registerTimeout) {
        this.registerTimeout = registerTimeout;
    }

    public AtomicBoolean getStart() {
        return start;
    }
}
