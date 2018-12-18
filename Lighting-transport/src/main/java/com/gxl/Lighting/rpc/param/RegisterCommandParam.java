package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.NotifyListener;
import com.gxl.Lighting.rpc.CommandParam;

public class RegisterCommandParam extends CommandParam {

    /**
     * 注册的 服务名
     */
    private String serviceName;

    /**
     * 当前服务器ip
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
