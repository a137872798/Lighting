package com.gxl.Lighting.consumer;

import com.gxl.Lighting.Application;
import com.gxl.Lighting.NotifyListener;

/**
 * 消费者 接口
 */
public interface Consumer extends Application {

    /**
     * 这里传入的 应该是 接口类
     */
    void addSubscribeService(Class<?> service);

    void addSubscribeServices(Class<?>... services);

    void subscribe(NotifyListener listener);

    void removeSubscribeService(Class<?> service);

    void removeSuscribeServices(Class<?>... services);

    boolean unSubscribe();

    /**
     * 返回 远程调用生成的代理对象 用户可以根据需要随时强转对象
     * @return
     */
    Object getService();
}
