package com.gxl.Lighting.consumer;

import com.gxl.Lighting.loadbalance.LoadBalance;

import java.util.HashMap;
import java.util.Map;

/**
 * 从 RPC 注解上获取的 元数据信息
 */
public class RPCMeta {

    /**
     * 代表该注解 是针对 哪个 类的
     */
    private Class<?> target;

    /**
     * 该方法调用的超时时间
     */
    private long timeout;

    /**
     * 使用的均衡负载策略
     */
    private Class<? extends LoadBalance> loadBalance;

    /**
     * 是否使用vip 端口
     */
    private boolean isVip;

    private String serviceName;

    private String invokeType;

    private String serialization;


    public Class<?> getTarget() {
        return target;
    }

    public void setTarget(Class<?> target) {
        this.target = target;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Class<? extends LoadBalance> getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(Class<? extends LoadBalance> loadBalance) {
        this.loadBalance = loadBalance;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(String invokeType) {
        this.invokeType = invokeType;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }
}
