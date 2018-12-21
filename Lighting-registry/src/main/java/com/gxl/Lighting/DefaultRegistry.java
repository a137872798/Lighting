package com.gxl.Lighting;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.meta.ServiceMeta;
import com.gxl.Lighting.meta.SubscribeMeta;
import com.gxl.Lighting.netty.DefaultServer;
import com.gxl.Lighting.netty.HeartBeatConfig;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.rpc.Listener;
import com.gxl.Lighting.rpc.RequestEnum;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.SubscribeCommandParam;
import com.gxl.Lighting.rpc.param.UnRegisterCommandParam;
import com.gxl.Lighting.rpc.param.UnSubscribeCommandParam;
import com.gxl.Lighting.processor.RegisterProcessor;
import com.gxl.Lighting.processor.SubscributeProcessor;
import com.gxl.Lighting.processor.UnRegisterProcessor;
import com.gxl.Lighting.processor.UnSubscributeProcessor;

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

    /**
     * 监听的是 服务级别 但是订阅的时候 可以一次针对多个 ServiceMeta 进行监听
     */
    private final ConcurrentMap<ServiceMeta, ConcurrentHashMap<String, NotifyListener>> notifyListeners = new ConcurrentHashMap<ServiceMeta, ConcurrentHashMap<String, NotifyListener>>();

    /**
     * 每个订阅者下面的 全部注册服务  这里value 必须是 registerMeta 因为 需要 服务的 地址
     */
    private final ConcurrentMap<SubscribeMeta, CopyOnWriteArrayList<RegisterMeta>> srInfo = new ConcurrentHashMap<SubscribeMeta, CopyOnWriteArrayList<RegisterMeta>>();

    private final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("registry scheduleExecutor", true));

    private final Server server;

    private final AtomicBoolean shutdown = new AtomicBoolean(false);

    public DefaultRegistry() {
        server = new DefaultServer();
        init();
    }

    public DefaultRegistry(HeartBeatConfig config) {
        server = new DefaultServer(config);
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
    }

    //从参数中抽取想要的 变量生成 订阅/注册信息后设置到任务队列中执行任务

    public void register(final RegisterCommandParam param) {
        RegisterMeta meta = param.getRegisterMeta();
        registers.add(meta);
        addRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
    }

    /**
     * 通知订阅者
     *
     * @param serviceMetas
     */
    private void notify(ServiceMeta[] serviceMetas) {
        for (ServiceMeta serviceMeta : serviceMetas) {
            ConcurrentMap<String, NotifyListener> listeners = notifyListeners.get(serviceMeta);
            if (listeners != null) {
                for (Map.Entry<String, NotifyListener> temp : listeners.entrySet()) {
                    String address = temp.getKey();
                    for (SubscribeMeta subscribute : srInfo.keySet()) {
                        if (subscribute.getAddress().equals(address)) {
                            temp.getValue().notify(srInfo.get(subscribute));
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
    }

    /**
     * 订阅指定服务
     *
     * @param param
     */
    public void subscribe(final SubscribeCommandParam param) {
        SubscribeMeta meta = param.getMeta();
        subscribes.add(meta);
        addListener(meta, param.getListener());
        addOldRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
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
     * @param listener
     */
    private void addListener(SubscribeMeta meta, NotifyListener listener) {
        ServiceMeta[] serviceMetas = meta.getServiceMeta();
        for (ServiceMeta serviceMeta : serviceMetas) {
            ConcurrentHashMap<String, NotifyListener> listenerMap = new ConcurrentHashMap<String, NotifyListener>();
            if (!notifyListeners.containsKey(serviceMeta)) {
                ConcurrentHashMap<String, NotifyListener> old = notifyListeners.putIfAbsent(serviceMeta, listenerMap);
                if (old != null) {
                    listenerMap = old;
                }
                listenerMap.put(meta.getAddress(), listener);
            } else {
                notifyListeners.get(serviceMeta).put(meta.getAddress(), listener);
            }
        }
    }

    /**
     * 移除监听器
     */
    private void removeListener(SubscribeMeta meta) {
        ServiceMeta[] serviceMetas = meta.getServiceMeta();
        for (ServiceMeta serviceMeta : serviceMetas) {
            ConcurrentMap<String, NotifyListener> listener = notifyListeners.get(meta);
            listener.remove(meta.getAddress());
            if(listener.size() == 0){
                notifyListeners.remove(meta);
        }
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
