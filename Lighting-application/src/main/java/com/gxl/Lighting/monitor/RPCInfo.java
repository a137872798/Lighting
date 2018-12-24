package com.gxl.Lighting.monitor;

public class RPCInfo {

    private String serviceName;

    private String provideAddress;

    private String methodName;

    /**
     * 调用次数
     */
    private int times;

    private int successTimes;

    private int failTimes;

    /**
     * 平均调用耗时
     */
    private long averageTime;

    /**
     * 成功率
     */
    private int rate;

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getProvideAddress() {
        return provideAddress;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public int getSuccessTimes() {
        return successTimes;
    }

    public void setSuccessTimes(int successTimes) {
        this.successTimes = successTimes;
    }

    public int getFailTimes() {
        return failTimes;
    }

    public void setFailTimes(int failTimes) {
        this.failTimes = failTimes;
    }

    @Override
    public String toString() {
        return "RPCInfo{" +
                "serviceName='" + serviceName + '\'' +
                ", provideAddress='" + provideAddress + '\'' +
                ", methodName='" + methodName + '\'' +
                ", times=" + times +
                ", successTimes=" + successTimes +
                ", failTimes=" + failTimes +
                ", averageTime=" + averageTime +
                ", rate=" + rate +
                '}';
    }

    public void setProvideAddress(String provideAddress) {
        this.provideAddress = provideAddress;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getAverageTime() {
        return averageTime;
    }

    public void setAverageTime(long averageTime) {
        this.averageTime = averageTime;
    }

    public int getRate() {
        return rate;
    }

    public void setRate(int rate) {
        this.rate = rate;
    }
}
