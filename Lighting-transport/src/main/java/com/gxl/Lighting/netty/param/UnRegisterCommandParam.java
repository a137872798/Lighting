package com.gxl.Lighting.netty.param;

import com.gxl.Lighting.netty.meta.RegisterMeta;
import com.gxl.Lighting.netty.CommandParam;

public class UnRegisterCommandParam implements CommandParam {

    private String serviceName;

    private RegisterMeta meta;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public RegisterMeta getMeta() {
        return meta;
    }

    public void setMeta(RegisterMeta meta) {
        this.meta = meta;
    }

    @Override
    public String toString() {
        return "UnRegisterCommandParam{" +
                "serviceName='" + serviceName + '\'' +
                ", meta=" + meta +
                '}';
    }
}
