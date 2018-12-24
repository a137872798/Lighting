package com.gxl.Lighting.netty.param;

import com.gxl.Lighting.netty.CommandParam;

public class CollectCommandParam implements CommandParam {

    /**
     * 本次调用是否成功
     */
    private boolean isSuccess;

    /**
     * 调用耗时
     */
    private long time;

    /**
     * 调用的 服务名
     */
    private String serviceName;

    /**
     * 调用的方法名
     */
    private String methodName;

    /**
     * 访问的 服务地址
     */
    private String address;

    public boolean isSuccess() {
        return isSuccess;
    }

    public void setSuccess(boolean success) {
        isSuccess = success;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "CollectCommandParam{" +
                "isSuccess=" + isSuccess +
                ", time=" + time +
                ", serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
