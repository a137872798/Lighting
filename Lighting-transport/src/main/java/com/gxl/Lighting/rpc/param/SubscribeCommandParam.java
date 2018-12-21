package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.NotifyListener;
import com.gxl.Lighting.meta.SubscribeMeta;
import com.gxl.Lighting.rpc.CommandParam;

public class SubscribeCommandParam implements CommandParam {

    /**
     * 服务名  可以 ， 拆分
     */
    private String serviceName;

    private SubscribeMeta meta;

    /**
     * 回调
     */
    private NotifyListener listener;

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

    public NotifyListener getListener() {
        return listener;
    }

    public void setListener(NotifyListener listener) {
        this.listener = listener;
    }
}
