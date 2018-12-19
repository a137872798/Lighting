package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.NotifyListener;
import com.gxl.Lighting.rpc.CommandParam;

public class SubscributeCommandParam extends CommandParam {

    /**
     * 服务名  可以 ， 拆分
     */
    private String serviceName;

    private int version;

    /**
     * 订阅者的地址
     */
    private String address;

    /**
     * 当订阅的该服务
     */
    private NotifyListener listener;

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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public NotifyListener getListener() {
        return listener;
    }

    public void setListener(NotifyListener listener) {
        this.listener = listener;
    }
}
