package com.gxl.Lighting.netty.param;

import com.gxl.Lighting.netty.NotifyListener;
import com.gxl.Lighting.netty.meta.SubscribeMeta;
import com.gxl.Lighting.netty.CommandParam;

public class SubscribeCommandParam implements CommandParam {

    /**
     * 服务名  可以 ， 拆分
     */
    private String serviceName;

    private SubscribeMeta meta;

    @Override
    public String toString() {
        return "SubscribeCommandParam{" +
                "serviceName='" + serviceName + '\'' +
                ", meta=" + meta +
                '}';
    }

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
