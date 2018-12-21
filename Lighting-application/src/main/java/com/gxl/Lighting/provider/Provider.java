package com.gxl.Lighting.provider;

import com.gxl.Lighting.Application;
import com.gxl.Lighting.rpc.RemotingSendException;
import com.gxl.Lighting.rpc.RemotingTimeoutException;

import java.net.UnknownHostException;

/**
 * 服务提供者接口
 */
public interface Provider extends Application {

    void addPublishService(Class<?> o);

    void addPublishServices(Class<?>... o);

    void publish() throws UnknownHostException;

    void removePublishService(Class<?> o);

    void removePublishServices(Class<?>... o);
}

