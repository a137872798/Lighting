package com.gxl.Lighting;

public interface Application {

    void shutdownGracefully();

    /**
     * 清空当前设置的 各种参数和属性
     */
    void reset();

    void start();

    String[] getRegistryAddresses();

    void setRegistryAddresses(String[] address);

    String getMonitorAddress();

    void setMonitorAddress(String address);
}
