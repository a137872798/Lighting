package com.gxl.Lighting.netty;

import com.gxl.Lighting.rpc.*;

/**
 * RPC 通信的客户端接口
 */
public interface Client extends Remoting{

    Connection connect(String address);

    /**
     * 调用服务端方法  返回的结果 被封装成result 对象
     * @return
     */
    RPCResult invokeSync(Request request);

    /**
     * 基本是用来做心跳检测的
     * @param request
     */
    void oneWay(Request request);

    /**
     * 异步调用 通过监听器触发回调 返回一个future 对象
     * @param request
     * @param listener
     */
    RPCFuture invokeASync(Request request, Listener listener);

    /**
     * 向注册中心 订阅服务
     * @param listener
     */
    void subscribute(Listener listener);

    int getConnectionTimeout();

    void setConnectionTimeout(int timeout);

    boolean getAutoReconnection();

    void setAutoReconnection(boolean autoReconnection);

    boolean isConnected();

}
