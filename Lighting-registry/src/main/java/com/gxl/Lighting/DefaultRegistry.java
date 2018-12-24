package com.gxl.Lighting;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.*;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.enums.SerializationEnum;
import com.gxl.Lighting.netty.meta.RegisterMeta;
import com.gxl.Lighting.netty.meta.ServiceMeta;
import com.gxl.Lighting.netty.meta.SubscribeMeta;
import com.gxl.Lighting.netty.param.*;
import com.gxl.Lighting.processor.RegisterProcessor;
import com.gxl.Lighting.processor.SubscributeProcessor;
import com.gxl.Lighting.processor.UnRegisterProcessor;
import com.gxl.Lighting.processor.UnSubscributeProcessor;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 注册中心
 */
public class DefaultRegistry implements Registry {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRegistry.class);

    /**
     * 存放 注册中心任务的队列
     */
    private final LinkedBlockingQueue<Runnable> task = new LinkedBlockingQueue<Runnable>();

    /**
     * 执行 注册中心的 各种任务
     */
    private final ExecutorService registryExecutor = Executors.newSingleThreadExecutor(new NamedThreadFactory("registry.executor", true));

    /**
     * 存放注册中心所有的 注册者
     */
    private final ConcurrentHashSet<RegisterMeta> registers = new ConcurrentHashSet<RegisterMeta>();

    private final ConcurrentHashSet<SubscribeMeta> subscribes = new ConcurrentHashSet<SubscribeMeta>();

    private static final int DEFAULT_PORT = 102;

    /**
     * 监听的是 服务级别 但是订阅的时候 可以一次针对多个 ServiceMeta 进行监听
     */
    private final ConcurrentMap<ServiceMeta, CopyOnWriteArrayList<String>> notifyAddresses = new ConcurrentHashMap<ServiceMeta, CopyOnWriteArrayList<String>>();

    /**
     * 每个订阅者下面的 全部注册服务  这里value 必须是 registerMeta 因为 需要 服务的 地址
     */
    private final ConcurrentMap<SubscribeMeta, CopyOnWriteArrayList<RegisterMeta>> srInfo = new ConcurrentHashMap<SubscribeMeta, CopyOnWriteArrayList<RegisterMeta>>();

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("registry scheduleExecutor", true));

    private final Server server;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public DefaultRegistry() {
        server = new DefaultServer(DEFAULT_PORT);
        init();
    }

    public DefaultRegistry(HeartBeatConfig config) {
        server = new DefaultServer(DEFAULT_PORT, config);
        init();
    }

    private void init() {
        server.getProcessorManager().registerProcessor(RequestEnum.REGISTRY, new RegisterProcessor(this));
        server.getProcessorManager().registerProcessor(RequestEnum.SUBSCRIBE, new SubscributeProcessor(this));
        server.getProcessorManager().registerProcessor(RequestEnum.UNREGISTRY, new UnRegisterProcessor(this));
        server.getProcessorManager().registerProcessor(RequestEnum.UNSUBSCRIBE, new UnSubscributeProcessor(this));
    }

    public void start() {
        server.start();
        logger.info("注册中心启动成功");
    }

    //从参数中抽取想要的 变量生成 订阅/注册信息后设置到任务队列中执行任务

    public void register(final RegisterCommandParam param) {
        RegisterMeta meta = param.getRegisterMeta();
        registers.add(meta);
        addRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
        logger.info("成功注册" + param);
    }

    /**
     * 通知订阅者
     *
     * @param serviceMetas
     */
    private void notify(ServiceMeta[] serviceMetas) {
        for (ServiceMeta serviceMeta : serviceMetas) {
            List<String> listeners = notifyAddresses.get(serviceMeta);
            if (listeners != null) {
                for (String address : listeners) {
                    for (SubscribeMeta subscribute : srInfo.keySet()) {
                        if (subscribute.getAddress().equals(address)) {
                            if (server.getClientMap().get(address) != null) {
                                ClientMeta client = server.getClientMap().get(address);
                                NotifyCommandParam param = new NotifyCommandParam();
                                param.setServices(srInfo.get(subscribute));
                                Request request = Request.createRequest(RequestEnum.NOTIFY, param);
                                request.setSerialization(SerializationEnum.JSON.getSerialization());
                                request.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
                                client.getChannel().writeAndFlush(request).addListener(new ChannelFutureListener() {
                                    @Override
                                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                        if (channelFuture.cause() == null) {
                                            logger.info("通知客户端当前可使用的服务提供者");
                                        }else{
                                            logger.warn("通知客户端可用服务提供者时 失败");
                                        }
                                    }
                                });
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 在订阅者 还不存在的 情况下 就不需要添加了 所以没有增加这个键值对
     *
     * @param meta
     */
    private void addRegister(RegisterMeta meta) {
        ServiceMeta[] metas = meta.getServiceMeta();
        for (SubscribeMeta subscribute : srInfo.keySet()) {
            //直接跳跃到这里 只要有一个能对应上 就代表这个订阅者可以获取到 这个 提供者的 信息 避免重复添加
            loop:
            //该订阅者订阅的 全部 服务
            for (ServiceMeta serviceMeta : subscribute.getServiceMeta()) {
                for (ServiceMeta registerMeta : metas) {
                    if (serviceMeta.equals(registerMeta)) {
                        srInfo.get(subscribute).add(meta);
                        continue loop;
                    }
                }
            }
        }
    }

    /**
     * 从订阅者保存的 订阅信息中移除该 服务提供者
     *
     * @param meta
     */
    private void removeRegister(RegisterMeta meta) {
        for (Map.Entry<SubscribeMeta, CopyOnWriteArrayList<RegisterMeta>> entry : srInfo.entrySet()) {
            for (RegisterMeta registerMeta : entry.getValue()) {
                if (registerMeta.equals(meta)) {
                    entry.getValue().remove(meta);
                }
            }
        }
    }

    /**
     * 某个服务提供者注销
     *
     * @param param
     */
    public void unregister(final UnRegisterCommandParam param) {
        RegisterMeta meta = param.getMeta();
        registers.remove(meta);
        removeRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
        logger.info("成功注销" + param);
    }

    /**
     * 订阅指定服务
     *
     * @param param
     */
    public void subscribe(final SubscribeCommandParam param) {
        SubscribeMeta meta = param.getMeta();
        subscribes.add(meta);
        addListenerAddress(meta);
        addOldRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
        logger.info("成功订阅" + param);
    }


    /**
     * 首次订阅时 应该将已经存在的对应的 服务提供者 添加进来 如果还不存在键值对  使用 putifabsent
     *
     * @param meta
     */
    private void addOldRegister(SubscribeMeta meta) {
        for (RegisterMeta temp : registers) {
            ServiceMeta[] subscributeMetas = meta.getServiceMeta();
            ServiceMeta[] registerMetas = temp.getServiceMeta();
            //避免重复添加
            loop:
            for (ServiceMeta serviceMeta : subscributeMetas) {
                for (ServiceMeta registerMeta : registerMetas) {
                    if (registerMeta.equals(serviceMeta)) {
                        if (!srInfo.containsKey(meta)) {
                            CopyOnWriteArrayList<RegisterMeta> list = new CopyOnWriteArrayList<RegisterMeta>();
                            CopyOnWriteArrayList old = srInfo.putIfAbsent(meta, list);
                            if (old != null) {
                                list = old;
                            }
                            list.add(temp);
                        } else {
                            srInfo.get(meta).add(temp);
                        }
                        continue loop;
                    }
                }
            }
        }
    }

    /**
     * 为指定服务设置 监听器
     *
     * @param meta
     */
    private void addListenerAddress(SubscribeMeta meta) {
        ServiceMeta[] serviceMetas = meta.getServiceMeta();
        for (ServiceMeta serviceMeta : serviceMetas) {
            CopyOnWriteArrayList<String> listenerAddress = new CopyOnWriteArrayList<>();
            if (!notifyAddresses.containsKey(serviceMeta)) {
                CopyOnWriteArrayList<String> old = notifyAddresses.putIfAbsent(serviceMeta, listenerAddress);
                if (old != null) {
                    listenerAddress = old;
                }
                listenerAddress.add(meta.getAddress());
            } else {
                listenerAddress.add(meta.getAddress());
            }
        }
    }

    /**
     * 移除监听器
     */
    private void removeListener(SubscribeMeta meta) {
        ServiceMeta[] serviceMetas = meta.getServiceMeta();
        for (ServiceMeta serviceMeta : serviceMetas) {
            CopyOnWriteArrayList<String> notifyAddress = notifyAddresses.get(meta);
            notifyAddress.remove(meta.getAddress());
        }
    }

    /**
     * 取消订阅
     *
     * @param param
     */
    public void unsubscribe(final UnSubscribeCommandParam param) {
        SubscribeMeta meta = param.getMeta();
        subscribes.remove(meta);
        removeListener(meta);
        removeSubscribute(meta);
        logger.info("成功取消订阅" + param);
    }

    /**
     * 移除订阅者信息
     *
     * @param meta
     */
    private void removeSubscribute(SubscribeMeta meta) {
        for (SubscribeMeta temp : srInfo.keySet()) {
            if (temp.equals(meta)) {
                srInfo.remove(temp);
            }
        }
    }


    public void shutdownGracefully() {
        if (!shutdown.getAndSet(true)) {
            registryExecutor.shutdown();
            server.shutdownGracefully();
        }
    }

    public List<RegisterMeta> registers() {
        return registers.toList();
    }

    public List<SubscribeMeta> subscributes() {
        return subscribes.toList();
    }
}
