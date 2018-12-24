package com.gxl.Lighting.netty.meta;

import java.util.Arrays;

/**
 * 为什么要做出这个 分级 因为 注册中心 有些订阅是针对 服务级别的 有些却需要用到 具体的地址 为了解耦 需要这个类
 */
public class ServiceMeta {

    private String serviceName;

    @Override
    public String toString() {
        return "ServiceMeta{" +
                "serviceName='" + serviceName + '\'' +
                ", methodMeta=" + Arrays.toString(methodMeta) +
                ", version=" + version +
                '}';
    }

    /**
     * 该服务下 方法级别的 参数
     */
    private MethodMeta[] methodMeta;

    private int version;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public MethodMeta[] getMethodMeta() {
        return methodMeta;
    }

    public void setMethodMeta(MethodMeta[] methodMeta) {
        this.methodMeta = methodMeta;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceMeta that = (ServiceMeta) o;

        if (version != that.version) return false;
        if (serviceName != null ? !serviceName.equals(that.serviceName) : that.serviceName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(methodMeta, that.methodMeta);
    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(methodMeta);
        result = 31 * result + version;
        return result;
    }
}
