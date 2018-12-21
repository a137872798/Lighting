package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.meta.SubscribeMeta;
import com.gxl.Lighting.rpc.CommandParam;

/**
 * 消费者取消订阅使用的参数
 */
public class UnSubscribeCommandParam implements CommandParam {

    /**
     * 可以 ， 拆分
     */
    private String serviceName;

    private SubscribeMeta meta;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public SubscribeMeta getMeta() {
        return meta;
    }

    public void setMeta(SubscribeMeta meta) {
        this.meta = meta;
    }
}
