package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.rpc.CommandParam;

public class UnRegisterCommandParam extends CommandParam {

    private String address;

    private String serviceName;

    private int version;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
}
