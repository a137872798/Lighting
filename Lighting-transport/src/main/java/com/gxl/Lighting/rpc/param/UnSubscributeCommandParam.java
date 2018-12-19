package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.rpc.CommandParam;

/**
 * 消费者取消订阅使用的参数
 */
public class UnSubscributeCommandParam extends CommandParam {

    /**
     * 可以 ， 拆分
     */
    private String serviceName;

    /**
     * 这样注册中心才知道 是 哪个消费者取消了订阅 不然 无法确认
     */
    private String address;

    private int version;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
