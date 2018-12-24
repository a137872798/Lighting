package com.gxl.Lighting.monitor;

import com.gxl.Lighting.netty.meta.RegisterMeta;
import com.gxl.Lighting.netty.Request;

import java.util.List;

public class StatisticContext {
    private long startTime;

    private String serviceName;

    private String methodName;

    private String address;

    private long timeout;

    private Request request;

    private List<RegisterMeta> metas;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public long getTimeout() {
        return timeout;
    }

    public List<RegisterMeta> getMetas() {
        return metas;
    }

    public void setMetas(List<RegisterMeta> metas) {
        this.metas = metas;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
}
