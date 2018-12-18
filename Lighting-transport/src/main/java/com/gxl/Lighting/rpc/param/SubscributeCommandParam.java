package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.NotifyListener;
import com.gxl.Lighting.rpc.CommandParam;

public class SubscributeCommandParam extends CommandParam{

    /**
     * 服务名
     */
    private String serivceName;

    private int verison;

    /**
     * 订阅者的地址
     */
    private String address;

    /**
     * 当订阅的该服务
     */
    private NotifyListener listener;

    public String getSerivceName() {
        return serivceName;
    }

    public void setSerivceName(String serivceName) {
        this.serivceName = serivceName;
    }

    public int getVerison() {
        return verison;
    }

    public void setVerison(int verison) {
        this.verison = verison;
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
