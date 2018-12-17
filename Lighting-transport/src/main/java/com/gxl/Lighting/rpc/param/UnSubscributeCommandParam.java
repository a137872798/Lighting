package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.rpc.CommandParam;

/**
 * 消费者取消订阅使用的参数
 */
public class UnSubscributeCommandParam extends CommandParam {

    /**
     * 取消对哪些服务的订阅  TODO ，分隔
     */
    private String serviceName;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
