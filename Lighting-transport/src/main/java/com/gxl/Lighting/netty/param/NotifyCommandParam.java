package com.gxl.Lighting.netty.param;

import com.gxl.Lighting.netty.CommandParam;
import com.gxl.Lighting.netty.meta.RegisterMeta;

import java.util.List;

public class NotifyCommandParam implements CommandParam {

    private List<RegisterMeta> services;

    public List<RegisterMeta> getServices() {
        return services;
    }

    public void setServices(List<RegisterMeta> services) {
        this.services = services;
    }

    @Override
    public String toString() {
        return "NotifyCommandParam{" +
                "services=" + services +
                '}';
    }
}
