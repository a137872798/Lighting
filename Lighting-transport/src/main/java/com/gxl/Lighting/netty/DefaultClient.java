package com.gxl.Lighting.netty;

import com.gxl.Lighting.NamedThreadFactory;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.SubscributeMeta;
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
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;
import io.netty.util.concurrent.*;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultClient implements Client {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultClient.class);

    private Bootstrap bootstrap;

    private int connectionTimeout;

    private static final int DEFAULT_CONNECTION_TIMEOUT = 3000;

    private Timer timer = new HashedWheelTimer(new NamedThreadFactory("connection.timer", true));

    private Timer cleanTimer = new HashedWheelTimer(new NamedThreadFactory("clean.timer", true));

    /**
     * 心跳检测参数对象
     */
    private HeartBeatConfig config;

    /**
     * 该客户端下处理所有请求的对象
     */
    private ProcessorManager processorManager;

    /**
     * 是否被关闭
     */
    private volatile boolean closed;

    /**
     * 作为 提供者 管理关联 注册中心的 所有channel
     */
    private final Map<String, Channel> registryChannel = new HashMap<String, Channel>();

    /**
     * 维护 同步请求的结果对象  并配合后台线程清理 过时的 response 代表连接超时
     */
    private final ConcurrentMap<Long, ResponseFuture> responseTable = new ConcurrentHashMap<Long, ResponseFuture>();

    /**
     * 定时清理 response 对象
     */
    private final TimerTask cleanTask = new TimerTask() {
        public void run(Timeout timeout){

            for(Map.Entry<Long, ResponseFuture> entry : responseTable.entrySet()){
                if(entry.getValue().getCallback() != null){
                    //触发 异步超时的 异常
                    Response response = new Response(entry.getKey());
                    response.setSuccess(false);
                    response.setErrorMsg("请求超时");
                    response.setCause(new RemotingTimeoutException("请求超时"));
                    Callback callback = entry.getValue().getCallback();
                    callback.callback(response);
                } else {
                    entry.getValue().setResponse(null);
                }
                responseTable.remove(entry.getKey());
            }
            //设置下次任务
            timer.newTimeout(this, 3, TimeUnit.SECONDS);
        }
    };

    /**
     * 维护  到达服务器的长连接
     */
    private Channel channel;
    /**
     * 使用cpu2倍的线程数 并使用netty 中可以生成独占线程的 线程工厂
     */
    private final NioEventLoopGroup workerGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors() * 2, new DefaultThreadFactory("NettyClientWorker", true));


    public DefaultClient() {
        this(HeartBeatConfig.getDefaultWriteridletimeseconds(), HeartBeatConfig.getDefaultReaderidletimeseconds(), HeartBeatConfig.getDefaultAllidletimeseconds());
    }


    public DefaultClient(int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
        this(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds, true);
    }

    public DefaultClient(int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds, boolean autoReconnection) {
        checkParam(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        config = new HeartBeatConfig(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        //这样 processor 就可以 直接访问到 待处理的 response 对象了
        processorManager = new ProcessorManager(responseTable);
        init();
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

    public void connect(String address, int port) {
        final InetSocketAddress inetSocketAddress = new InetSocketAddress(address, port);
        final Bootstrap bootstrap = getBootstrap();
        bootstrap.remoteAddress(inetSocketAddress);
        synchronized (bootstrap) {
            bootstrap.handler(new ChannelInitializer() {
                protected void initChannel(Channel channel) throws Exception {
                    ChannelPipeline pipeline = channel.pipeline();
                    pipeline.addLast("decoder", new LightingDecoder())
                            .addLast("encoder", new LightingEncoder())
                            .addLast(new HeartBeatHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds(), true))
                            .addLast(new ConnectionHandler(inetSocketAddress, bootstrap, timer))
                            .addLast(new DispatchHandler(processorManager));
                }
            });
            ChannelFuture future = bootstrap.connect();
            if (future.awaitUninterruptibly().isSuccess()) {
                registryChannel.put(address + ":" + String.valueOf(port), future.channel());
            } else {
                logger.warn("连接到{}失败", address);
            }
        }
    }

    /**
     * 异步调用
     * @param address
     * @param request
     * @param callback
     * @param timeout
     */
    public void invokeASync(String address, final Request request, Callback callback, long timeout) {
        final Channel channel = registryChannel.get(address);
        if (channel == null) {
            logger.warn("没有找到{}对应的通道对象", address);
        }
        final ResponseFuture future = new ResponseFuture(request.getId(),callback,timeout);
        responseTable.put(request.getId(), future);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(channelFuture.isSuccess()){
                   future.setSendSuccess(true);
                } else {
                    reqeustFail(request.getId());
                }
            }
        });
    }

    /**
     * 在异步调用时 处理 发送失败的情况
     * @param id
     */
    private void reqeustFail(long id) {
        final ResponseFuture future = responseTable.get(id);
        if(future != null){
            future.setSendSuccess(false);
            Response response = new Response(id);
            response.setSuccess(false);
            response.setErrorMsg("请求超时");
            response.setCause(new RemotingTimeoutException("请求超时"));
            future.getCallback().callback(response);
        }
    }

    /**
     * 同步调用需要一个超时时间
     *
     * @param address
     * @param request
     * @param timeout
     * @return
     */
    public Response invokeSync(final String address, final Request request, long timeout) throws
            RemotingSendException, RemotingTimeoutException{
        long startTime = System.currentTimeMillis();
        final Channel channel = registryChannel.get(address);
        if (channel == null) {
            logger.warn("没有找到{}对应的通道对象", address);
        }
        final ResponseFuture future = new ResponseFuture(request.getId(), null, timeout);

        responseTable.put(request.getId(), future);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                if(!channelFuture.isSuccess()){
                    //这里代表写入就 失败了 直接返回 response
                    future.setSendSuccess(false);
                    responseTable.remove(request.getId());
                    logger.warn("往{}发送{}时失败", address, request.toString());
                    future.setCause(channelFuture.cause());
                    future.setResponse(null);
                }else {
                    future.setSendSuccess(true);
                    logger.debug("往{}发送{}成功,等待结果", address, request.toString());
                }
            }
        });

        //这里传入的 请求超时时间 是 针对本次请求的 后台定时器是针对整个client 请求的超时 如果请求时间超过 client 设定的时间 还是按照client来算
        //超时时 后台线程也会往这里设置 空结果
        long currentTime = System.currentTimeMillis();
        timeout = timeout - currentTime + startTime;
        Response response = future.waitResponseUnInterrupt(timeout, TimeUnit.SECONDS);
        responseTable.remove(future.getId());
        if(response == null){
            if(future.isSendSuccess()){
                throw new RemotingTimeoutException("请求" + address + "超时");
            } throw new RemotingSendException("发送数据时失败" + future.getCause());
        }
        return response;
    }

    /**
     * 单向发送 不需要结果
     * @param address
     * @param request
     */
    public void oneWay(String address, Request request) {
        final Channel channel = registryChannel.get(address);
        channel.writeAndFlush(request);
    }

    public void subscribute(SubscributeMeta meta, Listener listener) {

    }


    private void init() {
        bootstrap = new Bootstrap();
        //初始化 事件循环组 通道类型 设置默认的 内存分配器
        bootstrap.group(workerGroup).channel(NioSocketChannel.class).option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.TCP_NODELAY, true).option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT).option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectionTimeout == 0 ? DEFAULT_CONNECTION_TIMEOUT : connectionTimeout);
        cleanTimer.newTimeout(cleanTask, 3, TimeUnit.SECONDS);
    }


    public boolean isConnected() {
        Channel channel = getChannel();
        if (channel == null) {
            return false;
        }
        return channel.isActive();
    }

    public void oneWay(Request request) {

    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
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
        timer.stop();
        cleanTimer.stop();
        workerGroup.shutdownGracefully();
        logger.info("关闭客户端");
    }

    public ProcessorManager getProcessorManager() {
        return processorManager;
    }

    public void setProcessorManager(ProcessorManager manager) {
        this.processorManager = processorManager;
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
