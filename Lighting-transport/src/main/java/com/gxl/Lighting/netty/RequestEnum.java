package com.gxl.Lighting.netty;

/**
 * 请求类型
 */
public enum RequestEnum {

    /**
     * 将服务注册到注册中心
     */
    REGISTRY,
    /**
     * 将服务从注册中心上注销
     */
    UNREGISTRY,
    /**
     * 订阅注册中心上的某个服务
     */
    SUBSCRIBE,
    /**
     * 取消订阅
     */
    UNSUBSCRIBE,
    /**
     * 发送监控数据
     */
    COLLECT,
    /**
     * RPC调用
     */
    INVOKE,
    /**
     * 返回客户端可供选择的 服务提供者信息
     */
    NOTIFY;


    public static RequestEnum indexOf(int type){
        for(RequestEnum temp : RequestEnum.values()){
            if(temp.ordinal() == type){
                return temp;
            }
        }
        return null;
    }
}
