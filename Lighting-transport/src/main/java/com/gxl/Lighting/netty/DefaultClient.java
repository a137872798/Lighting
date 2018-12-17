package com.gxl.Lighting.netty;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.codec.LightingDecoder;
import com.gxl.Lighting.netty.codec.LightingEncoder;
import com.gxl.Lighting.netty.heartbeat.HeartBeatHandler;
import com.gxl.Lighting.rpc.*;
import com.gxl.Lighting.rpc.processor.ProcessorManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.*;

import java.net.InetSocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultClient implements Client {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultClient.class);

    private Bootstrap bootstrap;

    private int connectionTimeout;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;

    /**
     * 心跳检测参数对象
     */
    private HeartBeatConfig config;

    /**
     * 重连间隔时间
     */
    private int reconnectionInterval;

    /**
     * 该客户端下处理所有请求的对象
     */
    private ProcessorManager processorManager;

    /**
     * 默认重连间隔时间
     */
    private static final int DEFAULT_RECONNECTION_INTERVAL = 10;

    /**
     * 防止 duo'x
     */
    private final Lock connectionLock = new ReentrantLock();

    /**
     * 是否被关闭
     */
    private volatile boolean closed;

    /**
     * 维护  到达服务器的长连接
     */
    private Channel channel;
    /**
     * 使用cpu2倍的线程数 并使用netty 中可以生成独占线程的 线程工厂
     */
    private static final NioEventLoopGroup nioEventLoopGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new DefaultThreadFactory("NettyClientWorker", true));


    /**
     * 这里借鉴了 Dubbo 的 思想 在后台开启一个不断进行重连的 任务 但是我选择的实现不同 该对象只需要1条线程就能处理所有定时任务
     * 并且 线程上下文切换没有这么激烈 每次以一个tick 为单位 使得线程sleep指定时间
     */
    private static final HashedWheelTimer timer = new HashedWheelTimer();

    private static final DefaultEventLoop reconnectionExecutor = new DefaultEventLoop();

    /**
     * 关于该client的 定时重连
     */
    private ScheduledFuture reconnectionFuture;

    public DefaultClient() {
        this(DEFAULT_RECONNECTION_INTERVAL, HeartBeatConfig.getDefaultWriteridletimeseconds(), HeartBeatConfig.getDefaultReaderidletimeseconds(), HeartBeatConfig.getDefaultAllidletimeseconds(), true);
    }

    public DefaultClient(int reconnectionInterval) {
        this(reconnectionInterval, HeartBeatConfig.getDefaultWriteridletimeseconds(), HeartBeatConfig.getDefaultReaderidletimeseconds(), HeartBeatConfig.getDefaultAllidletimeseconds(), true);
    }

    public DefaultClient(int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(DEFAULT_RECONNECTION_INTERVAL, writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds, true);
    }

    public DefaultClient(int reconnectionInterval, int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds, boolean autoReconnection) {
        checkParam(reconnectionInterval, writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        config = new HeartBeatConfig(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        this.reconnectionInterval = reconnectionInterval;
    }

    private void checkParam(int reconnectionInterval, int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
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
        if (reconnectionInterval <= readerIdleTimeSeconds) {
            logger.debug("重连尝试间隔不应该小于心跳检测间隔");
            throw new IllegalArgumentException("重连尝试间隔不应该小于心跳检测间隔");
        }
    }

    public void connect(String address, int port) {
        bootstrap = new Bootstrap();
        //初始化 事件循环组 通道类型 设置默认的 内存分配器
        bootstrap.group(nioEventLoopGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .remoteAddress(new InetSocketAddress(address, port))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout == 0 ? DEFAULT_CONNECTION_TIMEOUT : connectionTimeout);

        bootstrap.handler(new ChannelInitializer() {
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast("decoder", new LightingDecoder())
                        .addLast("encoder", new LightingEncoder())
                        .addLast(new HeartBeatHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds(), true))
                        .addLast(new DispatchHandler());
            }
        });
        startConnectionTask();
    }

    /**
     * 开始启动后台 重连任务
     *
     */
    private void startConnectionTask() {
        logger.info("已经开启客户端定时重连任务");
        reconnectionFuture = reconnectionExecutor.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                if (!DefaultClient.this.isConnected()) {
                    ChannelFuture future = DefaultClient.this.getBootstrap().connect();
                    boolean result = future.awaitUninterruptibly(getConnectionTimeout(), TimeUnit.MILLISECONDS);
                    if (result && future.isSuccess()) {
                        Channel newChannel = future.channel();
                        try {
                            Channel oldChannel = DefaultClient.this.getChannel();
                            if (oldChannel != null) {
                                logger.info("在重新连接时 发现存在旧的连接将旧的连接关闭后 并创建了新的连接");
                                oldChannel.close();
                            }
                        } finally {
                            if (DefaultClient.this.isClosed()) {
                                logger.info("在创建新连接时失败 因为客户端被关闭了");
                                DefaultClient.this.channel = null;
                            } else {
                                DefaultClient.this.channel = newChannel;
                            }
                        }
                    }
                }
            }
        }, 0, getReconnectionInterval(), TimeUnit.SECONDS);
    }


    public int getReconnectionInterval() {
        return reconnectionInterval;
    }

    public void setReconnectionInterval(int reconnectionInterval) {
        this.reconnectionInterval = reconnectionInterval;
    }

    public boolean isConnected() {
        Channel channel = getChannel();
        if (channel == null) {
            return false;
        }
        return channel.isActive();
    }

    public RPCResult invokeSync(RPCRequest request) {
        return null;
    }

    public void oneWay(RPCRequest request) {

    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public RPCFuture invokeASync(RPCRequest request, Listener listener) {
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
     *
     * @param timeout
     */
    public void setConnectionTimeout(int timeout) {
        if (timeout <= 0) {
            this.connectionTimeout = 1000;
        }
        this.connectionTimeout = timeout;
    }

    public void start() {

    }

    public void shutdownGracefully() {
        //终止时 肯定要先停止重连定时任务  boolean 代表不可被打断
        reconnectionFuture.cancel(false);
        logger.info("关闭客户端");
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
