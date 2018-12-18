package com.gxl.Lighting.provider;

import com.gxl.Lighting.ConcurrentHashSet;
import com.gxl.Lighting.Version;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.Client;
import com.gxl.Lighting.netty.DefaultClient;
import com.gxl.Lighting.netty.DefaultServer;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.proxy.Invoker;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.RequestEnum;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.util.StringUtil;
import io.netty.channel.Channel;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultProvider implements Provider{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultProvider.class);

    /**
     * 提供者 作为 注册中心的客户端
     */
    private Client clients;

    /**
     * 连接到注册中心的所有channel
     */
    private ConcurrentHashMap<String, Channel> registryChannel;

    private Server server;

    private Server vipServer;

    private String monitorAddress;

    private String[] registryAddresses;

    /**
     * 封装了调用服务实现类的逻辑
     */
    private Invoker invoker;

    /**
     * 该服务提供者 暴露的服务
     */
    private ConcurrentHashSet<Object> exports = new ConcurrentHashSet<>();

    public DefaultProvider(){
        clients = new ConcurrentHashSet<Client>();
        server = new DefaultServer();
        vipServer = new DefaultServer();
    }

    @Override
    public void publishService(Object o){
        exports.add(o);
    }

    @Override
    public void publishServices(Object... o){
        for (Object temp : o){
            exports.add(o);
        }
    }

    /**
     * 启动服务提供者
     */
    public void start(){
        if(checkParam(monitorAddress, registryAddresses, exports)){
            invoker = createInvoker();
            server.start();
            vipServer.start();
            register();
        }
    }

    /**
     * 针对之前发布的所有服务 进行注销
     */
    @Override
    public void unPublish() {

    }

    @Override
    public void shutdownGracefully() {
        unPublish();
        for(Client client : clients){
            client.shutdownGracefully();
        }
        server.shutdownGracefully();
        vipServer.shutdownGracefully();
    }

    @Override
    public void reset() {
        clients.clear();
        monitorAddress = null;
        registryAddresses = null;
    }


    /**
     * 将 服务注册到注册中心
     */
    private void register() {
        ArrayList<String> serviceNames = new ArrayList<>();
        for(Object o : exports){
            serviceNames.add(o.getClass().getSuperclass().getSimpleName());
        }
        String serviceName = StringUtil.join(serviceNames.toArray(new String[0]),",");

        for(String registryAddress : registryAddresses){
            String[] address = StringUtil.split(registryAddress, ":");
            //创建并连接到
            Client client = new DefaultClient();
            clients.add(client);
            client.connect(address[0], Integer.valueOf(address[1]));

            RegisterCommandParam param = new RegisterCommandParam();
            param.setVersion(Version.version());
            param.setServiceName(serviceName);
            Request request = Request.createRequest(RequestEnum.REGISTRY, new RegisterCommandParam());
            request.
            client.invokeSync();
        }
    }

    private Invoker createInvoker() {
        return new Invoker(exports.toList());
    }

    /**
     * 启动服务前 检测 服务是否允许正常启动
     * @param monitorAddress
     * @param registryAddresses
     * @param exports
     */
    private boolean checkParam(String monitorAddress, String[] registryAddresses, ConcurrentHashSet<Object> exports) {
        if(StringUtil.isEmpty(monitorAddress)){
            logger.info("没有设置监控中心的地址 无法启动");
            return false;
        }
        if(registryAddresses.length == 0 || registryAddresses == null){
            logger.info("没有设置注册中心的地址 无法启动");
            return false;
        }
        if(exports.isEmpty()){
            logger.info("该服务提供者没有设置服务 无法启动");
            return false;
        }
        return true;
    }

    @Override
    public String[] getRegistryAddresses() {
        return registryAddresses;
    }

    @Override
    public void setRegistryAddresses(String[] addresses) {
        this.registryAddresses = registryAddresses;
    }

    @Override
    public String getMonitorAddress() {
        return monitorAddress;
    }

    @Override
    public void setMonitorAddress(String address) {
        this.monitorAddress = monitorAddress;
    }

    public ConcurrentHashSet<Client> getClients() {
        return clients;
    }

    public void setClients(ConcurrentHashSet<Client> clients) {
        this.clients = clients;
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
}
