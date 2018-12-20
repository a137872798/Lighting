package com.gxl.Lighting.provider;

import com.gxl.Lighting.Application;
import com.gxl.Lighting.rpc.RemotingSendException;
import com.gxl.Lighting.rpc.RemotingTimeoutException;

/**
 * 服务提供者接口
 */
public interface Provider extends Application {

    void addPublishService(Object o);

    void addPublishServices(Object... o);

    void publish();

    boolean unPublish(String registryAddress, String serviceName);

    void removePublishService(Object o);

    void removePublishServices(Object... o);
}

