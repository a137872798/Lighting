package com.gxl.Lighting.consumer;

import com.gxl.Lighting.Application;
import com.gxl.Lighting.NotifyListener;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.netty.Client;

import java.util.List;

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

    /**
     * 通过服务类 找到 对应服务的元数据
     * @param service
     * @return
     */
    RPCMeta getAnnotationInfo(Class<?> service);

    /**
     * 返回该消费者  目前设置的全部 服务
     * @return
     */
    Class<?>[] getServices();

    /**
     * 通过服务名获取可以访问 的  服务器对象
     * @param serviceName
     * @return
     */
    List<RegisterMeta> getRegisterInfo(String serviceName);

    Client getClient();
}
