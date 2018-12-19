package com.gxl.Lighting;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.meta.ServiceMeta;
import com.gxl.Lighting.meta.SubscributeMeta;
import com.gxl.Lighting.netty.DefaultServer;
import com.gxl.Lighting.netty.HeartBeatConfig;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.rpc.Listener;
import com.gxl.Lighting.rpc.RequestEnum;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.SubscributeCommandParam;
import com.gxl.Lighting.rpc.param.UnRegisterCommandParam;
import com.gxl.Lighting.rpc.param.UnSubscributeCommandParam;
import com.gxl.Lighting.rpc.processor.RegisterProcessor;
import com.gxl.Lighting.rpc.processor.SubscributeProcessor;
import com.gxl.Lighting.rpc.processor.UnRegisterProcessor;
import com.gxl.Lighting.rpc.processor.UnSubscributeProcessor;
import com.gxl.Lighting.util.ParamUtil;

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

    private final ConcurrentHashSet<SubscributeMeta> subscributes = new ConcurrentHashSet<SubscributeMeta>();

    /**
     * 针对服务级别的监听器
     */
    private final ConcurrentMap<ServiceMeta, ConcurrentHashMap<String, NotifyListener>> notifyListeners = new ConcurrentHashMap<ServiceMeta, ConcurrentHashMap<String, NotifyListener>>();

    /**
     * 每个订阅者下面的 全部注册服务
     */
    private final ConcurrentMap<SubscributeMeta, CopyOnWriteArrayList<RegisterMeta>> srInfo = new ConcurrentHashMap<SubscributeMeta, CopyOnWriteArrayList<RegisterMeta>>();

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
        server.getProcessorManager().registerProcessor(RequestEnum.SUBSCRIBUTE, new SubscributeProcessor(this));
        server.getProcessorManager().registerProcessor(RequestEnum.UNREGISTRY, new UnRegisterProcessor(this));
        server.getProcessorManager().registerProcessor(RequestEnum.UNSUBSCRIBUTE, new UnSubscributeProcessor(this));
    }

    public void start() {
        server.start();
    }

    //从参数中抽取想要的 变量生成 订阅/注册信息后设置到任务队列中执行任务

    public void register(final RegisterCommandParam param) {
        RegisterMeta meta = RegisterMeta.newMeta(param);
        registers.add(meta);
        addRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
    }

    /**
     * 通知订阅者
     *
     * @param meta
     */
    private void notify(ServiceMeta meta) {
        ConcurrentMap<String, NotifyListener> listeners = notifyListeners.get(meta);
        if (listeners != null) {
            for (Map.Entry<String, NotifyListener> temp : listeners.entrySet()) {
                String address = temp.getKey();
                for (SubscributeMeta subscribute : srInfo.keySet()) {
                    if (subscribute.getAddress().equals(address)) {
                        temp.getValue().notify(srInfo.get(subscribute));
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
        for (SubscributeMeta subscribute : srInfo.keySet()) {
            if (subscribute.getServiceMeta().getServiceName().equals(meta.getServiceMeta().getServiceName()) && subscribute.getServiceMeta().getVersion() == meta.getServiceMeta().getVersion()) {
                srInfo.get(subscribute).add(meta);
            }
        }
    }

    /**
     * 从订阅者保存的 订阅信息中移除该 服务提供者
     *
     * @param meta
     */
    private void removeRegister(RegisterMeta meta) {
        for (SubscributeMeta subscribute : srInfo.keySet()) {
            if (subscribute.getServiceMeta().getServiceName().equals(meta.getServiceMeta().getServiceName()) && subscribute.getServiceMeta().getVersion() == meta.getServiceMeta().getVersion()) {
                srInfo.get(subscribute).remove(meta);
            }
        }
    }

    /**
     * 某个服务提供者注销
     *
     * @param param
     */
    public void unregister(final UnRegisterCommandParam param) {
        RegisterMeta meta = RegisterMeta.newMeta(param);
        registers.remove(meta);
        removeRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
    }


    /**
     * 订阅指定服务
     *
     * @param param
     */
    public void subscribute(final SubscributeCommandParam param) {
        SubscributeMeta meta = SubscributeMeta.newMeta(param);
        subscributes.add(meta);
        addListener(meta, param.getListener());
        addOldRegister(meta);
        DefaultRegistry.this.notify(meta.getServiceMeta());
    }


    /**
     * 首次订阅时 应该将已经存在的对应的 服务提供者 添加进来 如果还不存在键值对  使用 putifabsent
     *
     * @param meta
     */
    private void addOldRegister(SubscributeMeta meta) {
        for (RegisterMeta temp : registers) {
            if (temp.getServiceMeta().equals(meta.getServiceMeta())) {
                if (!srInfo.containsKey(meta)) {
                    CopyOnWriteArrayList list = new CopyOnWriteArrayList();
                    CopyOnWriteArrayList old = srInfo.putIfAbsent(meta, list);
                    if (old != null) {
                        list = old;
                    }
                    list.add(temp);
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
    private void addListener(SubscributeMeta meta, NotifyListener listener) {
        ConcurrentHashMap<String, NotifyListener> listenerMap = new ConcurrentHashMap<String, NotifyListener>();
        if (!notifyListeners.containsKey(meta.getServiceMeta())) {
            ConcurrentHashMap<String, NotifyListener> old = notifyListeners.putIfAbsent(meta.getServiceMeta(), listenerMap);
            if (old != null) {
                listenerMap = old;
            }
        }
        listenerMap.put(meta.getAddress(), listener);
    }

    /**
     * 移除监听器
     */
    private void removeListener(SubscributeMeta meta) {
        ServiceMeta serviceMeta = meta.getServiceMeta();
        for (ServiceMeta temp : notifyListeners.keySet()) {
            if (temp.getServiceName().equals(serviceMeta.getServiceName())) {
                ConcurrentHashMap<String, NotifyListener> map = notifyListeners.get(temp);
                for (String address : map.keySet()) {
                    if (address.equals(meta.getAddress())) {
                        map.remove(address);
                    }
                }
            }
        }
    }

    /**
     * 取消订阅
     *
     * @param param
     */
    public void unsubscribute(final UnSubscributeCommandParam param) {
        SubscributeMeta meta = SubscributeMeta.newMeta(param);
        subscributes.remove(meta);
        removeListener(meta);
        removeSubscribute(meta);
    }

    /**
     * 移除订阅者信息
     *
     * @param meta
     */
    private void removeSubscribute(SubscributeMeta meta) {
        for (SubscributeMeta temp : srInfo.keySet()) {
            if (temp.getAddress().equals(meta.getAddress()) && temp.getServiceMeta().getServiceName().equals(meta.getServiceMeta().getServiceName())) {
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

    public List<SubscributeMeta> subscributes() {
        return subscributes.toList();
    }
}
