package com.gxl.Lighting.consumer;

import com.gxl.Lighting.ConcurrentHashSet;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.netty.Client;
import com.gxl.Lighting.netty.DefaultClient;
import com.gxl.Lighting.rpc.RPC;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultConsumer implements Consumer{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultConsumer.class);

    /**
     * 注意 这些接口 上是有 注解信息的  需要通过这个  来 获取
     */
    private ConcurrentHashSet<Class<?>> services = new ConcurrentHashSet<Class<?>>();

    /**
     * 获取到的 服务提供者信息
     */
    private ConcurrentMap<String, RegisterMeta> registerInfo = new ConcurrentHashMap<String, RegisterMeta>();

    /**
     * 连接到 注册中心 和  服务提供者的 客户端
     */
    private Client client;

    private AtomicBoolean start = new AtomicBoolean(false);

    private long subscributeTimeout;

    public DefaultConsumer(long subscributeTimeout){
        this.subscributeTimeout = subscributeTimeout;
        init();
    }

    private void init() {
        client = new DefaultClient();
    }


    public void subscributeService(Class<?> serviceName) {

    }

    public void subscributeServices(Class<?>... serviceName) {

    }

    public void unSubscribute() {

    }

    public void shutdownGracefully() {

    }

    public void reset() {

    }

    public void start() {
        if (start.compareAndSet(false, true)) {
            if (checkParam(services)){

            }
        }
    }

    /**
     * 检验需要 订阅的 接口信息是否正常
     * @param services
     */
    private boolean checkParam(ConcurrentHashSet<Class<?>> services) {
        if(services.size() == 0){
            logger.warn("订阅的服务列表不能为空");
            return false;
        }
        for(Class<?> clazz : services){
            if(!clazz.getClass().isInterface()){
                logger.warn(clazz.getClass().getSimpleName() + "不是接口 不能作为服务");
                return false;
            }
            boolean hasAnnotations = false;
            for(Method method : clazz.getDeclaredMethods()){
                if(method.isAnnotationPresent(RPC.class)){
                    hasAnnotations = true;
                    break;
                }
            }
            if(!hasAnnotations){
                logger.warn(clazz.getClass().getSimpleName() + "不存在可以被远程调用的方法(没有RPC注解标注的方法)");
            }
        }
        return true;
    }

    public String[] getRegistryAddresses() {
        return new String[0];
    }

    public void setRegistryAddresses(String[] address) {

    }

    public String getMonitorAddress() {
        return null;
    }

    public void setMonitorAddress(String address) {

    }
}
