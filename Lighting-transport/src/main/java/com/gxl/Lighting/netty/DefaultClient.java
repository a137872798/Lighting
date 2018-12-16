package com.gxl.Lighting.netty;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.codec.LightingDecoder;
import com.gxl.Lighting.netty.codec.LightingEncoder;
import com.gxl.Lighting.netty.heartbeat.HeartBeatHandler;
import com.gxl.Lighting.rpc.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.DefaultEventExecutor;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.LinkedBlockingQueue;

public class DefaultClient implements Client{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultClient.class);

    private Bootstrap bootstrap;

    private int connectionTimeout;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;

    /**
     * 判断当前是否处在连接状态
     */
    private boolean connected;

    /**
     * 心跳检测参数对象
     */
    private HeartBeatConfig config;

    /**
     * 当连接断开时 是否自动重连
     */
    private boolean autoReconnection;

    /**
     * 重连间隔时间
     */
    private int reconnectionInterval;

    /**
     * 维护  到达服务器的长连接
     */
    private Channel channel;
    /**
     * 使用cpu2倍的线程数 并使用netty 中可以生成独占线程的 线程工厂
     */
    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()*2,
            new DefaultThreadFactory("NettyClientWorker",true));


    /**
     * 这里借鉴了 Dubbo 的 思想 在后台开启一个不断进行重连的 任务 但是我选择的实现不同 该对象只需要1条线程就能处理所有定时任务
     * 并且 线程上下文切换没有这么激烈 每次以一个tick 为单位 使得线程sleep指定时间
     */
    private static final HashedWheelTimer timer = new HashedWheelTimer();

    DefaultClient(){
        this(HeartBeatConfig.getDefaultWriteridletimeseconds(), HeartBeatConfig.getDefaultReaderidletimeseconds(), HeartBeatConfig.getDefaultAllidletimeseconds(), true);
    }

    DefaultClient(int writerIdleTimeNanos, int readerIdleTimeNanos, int allIdleTimeNanos){
        this(writerIdleTimeNanos, readerIdleTimeNanos, allIdleTimeNanos, true);
    }

    DefaultClient(int writerIdleTimeNanos, int readerIdleTimeNanos, int allIdleTimeNanos, boolean autoReconnection){
        config = new HeartBeatConfig(writerIdleTimeNanos, readerIdleTimeNanos, allIdleTimeNanos);
        autoReconnection = true;
    }

    public Connection connect(String address) {
        bootstrap = new Bootstrap();
        //初始化 事件循环组 通道类型 设置默认的 内存分配器
        bootstrap.group(nioEventLoopGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout == 0? DEFAULT_CONNECTION_TIMEOUT : connectionTimeout);

        bootstrap.handler(new ChannelInitializer() {
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decoder", new LightingDecoder()).addLast("encoder", new LightingEncoder())
                        .addLast(new HeartBeatHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds(),true))
                        .addLast();
            }
        });

        //TODO 连接不能是单次 的  需要考虑 连接失败的 情况 所以需要一个可以不断尝试 重连的对象
        startConnectionTask(bootstrap);

        return null;
    }

    /**
     * 开始启动后台 重连任务
     * @param bootstrap
     */
    private void startConnectionTask(Bootstrap bootstrap) {
        timer.
    }


    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public RPCResult invokeSync(Request request) {
        return null;
    }

    public void oneWay(Request request) {

    }

    public RPCFuture invokeASync(Request request, Listener listener) {
        return null;
    }

    public RPCResult invokeSync(RPCParam param) {
        return null;
    }

    public RPCFuture invokeASync(RPCParam param, Listener listener) {
        return null;
    }

    public void subscribute(Listener listener) {

    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * 太小的 值 不让设置
     * @param timeout
     */
    public void setConnectionTimeout(int timeout) {
        if(timeout <= 0){
            this.connectionTimeout = 1000;
        }
        this.connectionTimeout = timeout;
    }

    public boolean getAutoReconnection() {
        return false;
    }

    public void setAutoReconnection(boolean autoReconnection) {

    }

    public void start() {

    }

    public void shutdownGracefully(ShutdownHook hook) {

    }

    public void shutdown() {

    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public HeartBeatConfig getConfig() {
        return config;
    }

    public void setConfig(HeartBeatConfig config) {
        this.config = config;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
