package com.gxl.Lighting.rpc;

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
    SUBSCRIBUTE,
    /**
     * 取消订阅
     */
    UNSUBSCRIBUTE,
    /**
     * 心跳检测
     */
    HEARTBEAT,
    /**
     * RPC调用
     */
    INVOKE;

    public static RequestEnum indexOf(int type){
        for(RequestEnum temp : RequestEnum.values()){
            if(temp.ordinal() == type){
                return temp;
            }
        }
        return null;
    }
}
