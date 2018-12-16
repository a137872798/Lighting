package com.gxl.Lighting.netty;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultServer implements Server{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultServer.class);

    private String[] registryAddress;

    private ServerBootstrap serverBootstrap;

    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2,
            new DefaultThreadFactory("NettyServerWorker",true));

    /**
     * 每个服务器 需要维护下面所有的客户端
     */
    private Map<Channel, ClientMeta> clientMap = new ConcurrentHashMap<Channel, ClientMeta>();

    /**
     * 每个客户端 最多允许 超时多少次
     */
    private Map<ClientMeta, Integer> heartBeatTimes = new ConcurrentHashMap<ClientMeta, Integer>();

    private static final int DEFAULT_HEARTBEATTIMES = 3;

    public void bind() {

    }

    public boolean register(String address) {
        return false;
    }

    public void setRegistryAddress(String address) {

    }

    public String[] getRegistryAddress() {
        return new String[0];
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

    }

    public void shutdownGracefully(ShutdownHook hook) {

    }

    public void shutdown() {

    }
}
