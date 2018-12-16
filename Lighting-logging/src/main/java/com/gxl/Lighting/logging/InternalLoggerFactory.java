package com.gxl.Lighting.logging;

/**
 * 日志工厂
 */
public abstract class InternalLoggerFactory {

    /**
     * 在使用单例模式创建时防止并发情况 创建多个对象 使用volatile 修饰
     */
    private static volatile InternalLoggerFactory defaultFactory;

    private static InternalLoggerFactory newDefaultFactory(String name){
        InternalLoggerFactory f;
        try{
            //先加载slf4j
            f = new Slf4JLoggerFactory();
            f.newInstance(name).debug("使用slf4j 作为日志框架");
        }catch (Throwable t) {
            //下面2个框架 不会用 测试没有通过
            try {
                f = new Log4J2LoggerFactory();
                f.newInstance(name).debug("使用log4j2 作为日志框架");
            } catch (Throwable t2) {
                f = new Log4J2LoggerFactory();
                f.newInstance(name).debug("使用log4j 作为日志框架");
            }
        }
        return f;
    }

    public static InternalLoggerFactory getDefaultFactory(){
        if(defaultFactory == null){
            defaultFactory = newDefaultFactory(InternalLoggerFactory.class.getName());
        }
        return defaultFactory;
    }

    public static void setDefaultFactory(InternalLoggerFactory factory){
        if(factory == null){
            throw new NullPointerException("默认工厂不能为null");
        }
        defaultFactory = factory;
    }

    public static InternalLogger getInstance(Class<?> clazz){
        return getInstance(clazz.getName());
    }

    private static InternalLogger getInstance(String name) {
        return getDefaultFactory().newInstance(name);
    }

    /**
     * 生成日志对象的核心功能由子类实现
     * @param name
     * @return
     */
    protected abstract InternalLogger newInstance(String name);
}
