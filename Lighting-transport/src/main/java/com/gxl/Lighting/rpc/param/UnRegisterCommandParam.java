package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.rpc.CommandParam;

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
}
