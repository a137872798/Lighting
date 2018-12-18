package com.gxl.Lighting.meta;

/**
 * 为什么要做出这个 分级 因为 注册中心 有些订阅是针对 服务级别的 有些却需要用到 具体的地址 为了解耦 需要这个类
 */
public class ServiceMeta {

    private String serviceName;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServiceMeta that = (ServiceMeta) o;

        if (version != that.version) return false;
        return serviceName != null ? serviceName.equals(that.serviceName) : that.serviceName == null;
    }

    @Override
    public int hashCode() {
        int result = serviceName != null ? serviceName.hashCode() : 0;
        result = 31 * result + version;
        return result;
    }
}
