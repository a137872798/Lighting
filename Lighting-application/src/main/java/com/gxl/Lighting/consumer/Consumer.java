package com.gxl.Lighting.consumer;

import com.gxl.Lighting.Application;

/**
 * 消费者 接口
 */
public interface Consumer extends Application {

    /**
     * 这里传入的 应该是 接口类
     * @param serviceName
     */
    void subscributeService(Class<?> serviceName);

    void subscributeServices(Class<?>... serviceName);

    void unSubscribute();

}
