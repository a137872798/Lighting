package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.NotifyListener;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.rpc.CommandParam;

public class RegisterCommandParam implements CommandParam {

    /**
     * 注册的元数据
     */
    private RegisterMeta registerMeta;

    /**
     * 注册的 服务名 ， 分割
     */
    private String serviceName;

    public RegisterMeta getRegisterMeta() {
        return registerMeta;
    }

    public void setRegisterMeta(RegisterMeta registerMeta) {
        this.registerMeta = registerMeta;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
}
