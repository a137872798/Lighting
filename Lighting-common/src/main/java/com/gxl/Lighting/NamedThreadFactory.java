package com.gxl.Lighting;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.util.ParamUtil;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂
 */
public class NamedThreadFactory implements ThreadFactory{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(NamedThreadFactory.class);

    private final AtomicInteger id_generator = new AtomicInteger(0);

    private final String name;

    private final int priority;

    private final boolean daemon;

    private final ThreadGroup group;

    public NamedThreadFactory(String name) {
        this(name, false, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String name, boolean daemon) {
        this(name, daemon, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String name, int priority) {
        this(name, false, priority);
    }

    public NamedThreadFactory(String name, boolean daemon, int priority) {
        this.name = name + " #";
        this.daemon = daemon;
        this.priority = priority;
        SecurityManager s = System.getSecurityManager();
        group = (s == null) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable runnable){
        ParamUtil.checkNotNull(runnable, "NamedThreadFactory 创建线程时 传入的Runable为空");

        String threadName = name + id_generator.getAndIncrement();
        Thread thread = new Thread(group, runnable, threadName);
        thread.setDaemon(daemon);
        thread.setPriority(priority);
        return thread;
    }
}
