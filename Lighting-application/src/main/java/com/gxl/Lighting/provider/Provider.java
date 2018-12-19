package com.gxl.Lighting.provider;

import com.gxl.Lighting.rpc.RemotingSendException;
import com.gxl.Lighting.rpc.RemotingTimeoutException;

/**
 * 服务提供者接口
 */
public interface Provider {

    String[] getRegistryAddresses();

    void setRegistryAddresses(String[] address);

    String getMonitorAddress();

    void setMonitorAddress(String address);

    void publishService(Object o);

    void publishServices(Object... o);

    void start();

    boolean unRegister();

    /**
     * 终止该服务提供者 现在已经启动的服务提供者如果要更换配置 必须要 先 终止 再启动
     */
    void shutdownGracefully();

    /**
     * 清空当前设置的 各种参数和属性
     */
    void reset();
}

