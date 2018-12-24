package com.gxl.Lighting.netty.param;

import com.gxl.Lighting.netty.meta.RegisterMeta;
import com.gxl.Lighting.netty.CommandParam;

public class RegisterCommandParam implements CommandParam {

    /**
     * 注册的元数据
     */
    private RegisterMeta registerMeta;

    @Override
    public String toString() {
        return "RegisterCommandParam{" +
                "registerMeta=" + registerMeta +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }

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
