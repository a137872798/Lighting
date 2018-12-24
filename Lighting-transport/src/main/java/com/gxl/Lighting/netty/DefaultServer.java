package com.gxl.Lighting.netty;

import com.gxl.Lighting.ConcurrentHashSet;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.codec.LightingDecoder;
import com.gxl.Lighting.netty.codec.LightingEncoder;
import com.gxl.Lighting.netty.heartbeat.HeartBeatHandler;
import com.gxl.Lighting.netty.processor.ProcessorManager;
import com.gxl.Lighting.util.AddressUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultServer implements Server{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultServer.class);

    /**
     * 这个 value 是不使用的
     */
    private Set<String> registryAddress = new ConcurrentHashSet<String>();

    /**
     * 服务器连接到本地的channel
     */
    private Channel channel;

    private ServerBootstrap serverBootstrap;

    private static final NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1,
            new DefaultThreadFactory("NettyServerBoss",true));

    private static final NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2,
            new DefaultThreadFactory("NettyServerWorker",true));

    private final HeartBeatConfig heartBeatConfig;

    /**
     * 每个服务器 需要维护下面所有的客户端
     */
    private Map<String, ClientMeta> clientMap = new ConcurrentHashMap<String, ClientMeta>();

    /**
     * 该服务器下 处理所有请求的对象
     */
    private ProcessorManager processorManager;

    /**
     * 每个客户端 最多允许 超时多少次
     */
    private Map<ClientMeta, Integer> heartBeatTimes = new ConcurrentHashMap<ClientMeta, Integer>();

    private static final int DEFAULT_HEARTBEATTIMES = 3;

    private final int port;

    private AtomicBoolean closed = new AtomicBoolean(true);

    public DefaultServer(){
        this(-1);
    }

    public DefaultServer(HeartBeatConfig config){
        this(-1, config);
    }

    public DefaultServer(int port){
        this(port, new HeartBeatConfig());
    }

    public DefaultServer(int port,HeartBeatConfig config)
    {
        this(port, HeartBeatConfig.getDefaultWriteridletimeseconds(), HeartBeatConfig.getDefaultReaderidletimeseconds()
                ,HeartBeatConfig.getDefaultAllidletimeseconds());
    }

    public DefaultServer(int port, int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
        checkParam(port, writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        this.port = port;
        heartBeatConfig = new HeartBeatConfig(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        processorManager = new ProcessorManager();
    }

    private void checkParam(int port, int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
        if(port > 65535 || port < -2){
            throw new IllegalArgumentException("端口号异常");
        }
        if (writerIdleTimeSeconds <= 0) {
            throw new IllegalArgumentException("writerIdleTimeSeconds 不能小于等于0");
        }
        if (readerIdleTimeSeconds <= 0) {
            throw new IllegalArgumentException("readerIdleTimeSeconds 不能小于等于0");
        }
        if (allIdleTimeSeconds <= 0) {
            throw new IllegalArgumentException("allIdleTimeSeconds 不能小于等于0");
        }
        if (readerIdleTimeSeconds - writerIdleTimeSeconds <= 0) {
            throw new IllegalArgumentException("基于当前框架 心跳检测的实现 一般写的时间间隔必须小于读的时间间隔");
        }
    }


    public void setRegistryAddress(String[] address) {
        registryAddress.clear();
        for(String temp : address){
            registryAddress.add(temp);
        }
    }

    public void addRegistryAddress(String address) {
        registryAddress.add(address);
    }

    public String[] getRegistryAddress() {
        return registryAddress.toArray(new String[]{});
    }

    public void addHeartBeatTimes(Channel channel) {
        ClientMeta meta = clientMap.get(AddressUtil.socketAddressToAddress((InetSocketAddress)channel.remoteAddress()));
        if(meta == null){
            logger.error("在服务器上没有找到地址为" + channel.remoteAddress() + "的客户端对象");
        } else {
            meta.addHeartBeatTimes();
            //根据心跳次数有没有超过一个限定值 断开连接
            ifReconnect(meta);
        }
    }

    public Map<String, ClientMeta> getClientMap() {
        return clientMap;
    }

    public static int getDefaultHeartbeattimes() {
        return DEFAULT_HEARTBEATTIMES;
    }

    private void ifReconnect(final ClientMeta meta) {
        heartBeatTimes.putIfAbsent(meta, 0);
        Integer maxHeartBeatTimes = heartBeatTimes.get(meta);
        if(maxHeartBeatTimes == null)
        if(meta.getHeartBeatTimes() >= maxHeartBeatTimes){
            logger.info("地址为" + meta.getAddress() + "的客户端对象 因为长时间未响应 已经断开了与该客户端的连接");
            meta.getChannel().close().addListener(new ChannelFutureListener() {
                public void operationComplete(ChannelFuture channelFuture) throws Exception {
                    //成功关闭后 移除 channel
                    clientMap.remove(channelFuture.channel());
                    heartBeatTimes.remove(meta);
                }
            });
        }
    }

    public Map<ClientMeta, Integer> getHeartBeatTimes() {
        return heartBeatTimes;
    }

    public void setHeartBeatTimes(Map<ClientMeta, Integer> heartBeatTimes) {
        this.heartBeatTimes = heartBeatTimes;
    }

    public void start() {
        if(closed.compareAndSet(true, false)) {
            serverBootstrap = new ServerBootstrap();
            final AdminHandler admin = new AdminHandler();
            clientMap = admin.getClientMap();
            serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup).channel(NioServerSocketChannel.class)
                    .handler(admin)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new ChannelInitializer<Channel>() {
                        protected void initChannel(Channel channel) throws Exception {
                            channel.pipeline().addLast("encoder", new LightingEncoder())
                                    .addLast("decoder", new LightingDecoder())
                                    .addLast(new HeartBeatHandler(heartBeatConfig.getReaderIdleTimeSeconds()
                                            , heartBeatConfig.getWriterIdleTimeSeconds()
                                            , heartBeatConfig.getAllIdleTimeSeconds(), false, DefaultServer.this))
                                    .addLast(new DispatchHandler(processorManager));
                        }
                    });
            //-1 代表没有默认端口号
            if(port != -1){
                serverBootstrap.localAddress(new InetSocketAddress("localhost", port));
            }else {
                serverBootstrap.localAddress(new InetSocketAddress("localhost", 8081));
            }
            ChannelFuture future = serverBootstrap.bind();
            future.syncUninterruptibly();
            this.channel = future.channel();
        }else {
            logger.info("服务器已经启动");
        }
    }

    public void shutdownGracefully() {
        if(closed.compareAndSet(false, true)) {
            try {
                if (channel != null) {
                    channel.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                Map<String, ClientMeta> map = getClientMap();
                for (Map.Entry<String, ClientMeta> temp : map.entrySet()) {
                    try {
                        temp.getValue().getChannel().close();
                    } catch (Exception e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                //关闭 group 对象
                if (serverBootstrap != null) {
                    bossEventLoopGroup.shutdownGracefully();
                    workerEventLoopGroup.shutdownGracefully();
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
            if (clientMap != null) {
                clientMap.clear();
            }
        }else {
            logger.info("服务器正在关闭");
        }
    }

    public void setRegistryAddress(Set<String> registryAddress) {
        this.registryAddress = registryAddress;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public ServerBootstrap getServerBootstrap() {
        return serverBootstrap;
    }

    public void setServerBootstrap(ServerBootstrap serverBootstrap) {
        this.serverBootstrap = serverBootstrap;
    }

    public void setClientMap(Map<String, ClientMeta> clientMap) {
        this.clientMap = clientMap;
    }

    public ProcessorManager getProcessorManager() {
        return processorManager;
    }

    public void setProcessorManager(ProcessorManager processorManager) {
        this.processorManager = processorManager;
    }


}
