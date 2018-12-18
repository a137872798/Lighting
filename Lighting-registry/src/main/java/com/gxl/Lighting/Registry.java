package com.gxl.Lighting;

import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.meta.SubscributeMeta;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.SubscributeCommandParam;
import com.gxl.Lighting.rpc.param.UnRegisterCommandParam;
import com.gxl.Lighting.rpc.param.UnSubscributeCommandParam;

import java.util.List;

/**
 * 注册中心接口
 */
public interface Registry {

    /**
     * 传入注册请求 参数 接受注册的 服务
     * @param param
     */
    void register(RegisterCommandParam param);

    /**
     * 注销
     * @param param
     */
    void unregister(UnRegisterCommandParam param);

    /**
     * 处理订阅请求
     * @param param
     */
    void subscribute(SubscributeCommandParam param);

    /**
     * 取消订阅
     * @param param
     */
    void unsubscribute(UnSubscributeCommandParam param);

    /**
     * 启动注册中心
     */
    void start();

    /**
     * 优雅关闭
     */
    void shutdownGracefully();

    /**
     * 获取 注册者信息
     * @return
     */
    List<RegisterMeta> registers();

    /**
     * 获取订阅者信息
     * @return
     */
    List<SubscributeMeta> subscributes();
}

