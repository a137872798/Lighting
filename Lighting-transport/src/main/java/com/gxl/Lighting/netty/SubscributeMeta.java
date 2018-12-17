package com.gxl.Lighting.netty;

/**
 * 向注册中心订阅的元数据
 */
public class SubscributeMeta {

    /**
     * 代表订阅的 服务名
     */
    private String serviceName;

    /**
     * 订阅的版本号
     */
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
}
