package com.gxl.Lighting.rpc;

import com.gxl.Lighting.netty.Request;

/**
 * 当调用失败时 保存失败信息
 */
public class RPCRetry {

    private String address;

    private long timeout;

    private Request request;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }
}
