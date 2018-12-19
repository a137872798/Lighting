package com.gxl.Lighting.provider;

import com.gxl.Lighting.Application;
import com.gxl.Lighting.rpc.RemotingSendException;
import com.gxl.Lighting.rpc.RemotingTimeoutException;

/**
 * 服务提供者接口
 */
public interface Provider extends Application {

    void publishService(Object o);

    void publishServices(Object... o);

    boolean unRegister();


}

