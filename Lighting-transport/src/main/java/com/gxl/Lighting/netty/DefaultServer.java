package com.gxl.Lighting.netty;

import com.gxl.Lighting.ConcurrentHashSet;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.codec.LightingDecoder;
import com.gxl.Lighting.netty.codec.LightingEncoder;
import com.gxl.Lighting.netty.heartbeat.HeartBeatHandler;
import com.gxl.Lighting.rpc.DispatchHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private Map<Channel, ClientMeta> clientMap = new ConcurrentHashMap<Channel, ClientMeta>();

    /**
     * 每个客户端 最多允许 超时多少次
     */
    private Map<ClientMeta, Integer> heartBeatTimes = new ConcurrentHashMap<ClientMeta, Integer>();

    private static final int DEFAULT_HEARTBEATTIMES = 3;

    public DefaultServer(){
        this(HeartBeatConfig.getDefaultWriteridletimeseconds(), HeartBeatConfig.getDefaultReaderidletimeseconds()
        ,HeartBeatConfig.getDefaultAllidletimeseconds());
    }

    public DefaultServer(int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
        checkParam(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        heartBeatConfig = new HeartBeatConfig(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
    }

    private void checkParam(int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
        if (writerIdleTimeSeconds <= 0) {
            logger.debug("writerIdleTimeNanos 不能小于等于0");
            throw new IllegalArgumentException("writerIdleTimeSeconds 不能小于等于0");
        }
        if (readerIdleTimeSeconds <= 0) {
            logger.debug("readerIdleTimeNanos 不能小于等于0");
            throw new IllegalArgumentException("readerIdleTimeSeconds 不能小于等于0");
        }
        if (allIdleTimeSeconds <= 0) {
            logger.debug("allIdleTimeNanos 不能小于等于0");
            throw new IllegalArgumentException("allIdleTimeSeconds 不能小于等于0");
        }
        if (readerIdleTimeSeconds - writerIdleTimeSeconds <= 0) {
            logger.debug("基于当前框架 心跳检测的实现 一般写的时间间隔必须小于读的时间间隔");
            throw new IllegalArgumentException("基于当前框架 心跳检测的实现 一般写的时间间隔必须小于读的时间间隔");
        }
    }

    public boolean register(String address) {
        return false;
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
        ClientMeta meta = clientMap.get(channel);
        if(meta == null){
            logger.error("在服务器上没有找到地址为" + channel.remoteAddress() + "的客户端对象");
        } else {
            meta.addHeartBeatTimes();
            //根据心跳次数有没有超过一个限定值 断开连接
            ifReconnect(meta);
        }
    }

    public Map<Channel, ClientMeta> getClientMap() {
        return clientMap;
    }

    public static int getDefaultHeartbeattimes() {
        return DEFAULT_HEARTBEATTIMES;
    }

    private void ifReconnect(final ClientMeta meta) {
        int maxHeartBeatTimes = heartBeatTimes.get(meta);
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
        serverBootstrap = new ServerBootstrap();
        final AdminHandler admin = new AdminHandler();
        clientMap = admin.getClientMap();
        serverBootstrap.group(bossEventLoopGroup, workerEventLoopGroup).channel(NioServerSocketChannel.class)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_REUSEADDR, true)
                .childHandler(new ChannelInitializer<Channel>() {
                    protected void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast("encoder", new LightingEncoder())
                                .addLast("encoder", new LightingDecoder())
                                .addLast(admin)
                                .addLast(new HeartBeatHandler(heartBeatConfig.getReaderIdleTimeSeconds()
                                        , heartBeatConfig.getWriterIdleTimeSeconds()
                                        , heartBeatConfig.getAllIdleTimeSeconds(), false, DefaultServer.this))
                                .addLast(new DispatchHandler());
                    }
                });
        ChannelFuture future = serverBootstrap.bind();
        future.syncUninterruptibly();
        this.channel = future.channel();
    }

    public void shutdownGracefully() {
        try {
            if (channel != null) {
                channel.close();
            }
        } catch (Exception e){
            logger.warn(e.getMessage(), e);
        }
        try{
            Map<Channel, ClientMeta> map = getClientMap();
            for(Channel temp : map.keySet()){
                try{
                    temp.close();
                }catch (Exception e){
                    logger.warn(e.getMessage(), e);
                }
            }
        }catch (Exception e){
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
        if(clientMap != null) {
            clientMap.clear();
        }
    }

    public void shutdown() {

    }

}
