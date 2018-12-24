package com.gxl.Lighting.netty;

import io.netty.channel.Channel;

import java.util.Map;

/**
 * RPC 通信的客户端接口
 */
public interface Client extends Remoting{

    void connect(String address, int port);

    /**
     * 调用服务端方法  返回的结果 被封装成result 对象
     * @return
     */
    Response invokeSync(String address, Request request, long timeout)throws InterruptedException
            , RemotingSendException, RemotingTimeoutException;

    /**
     * 发送数据到监控中心时 一般是使用这个
     * @param request
     */
    void oneWay(String address, Request request);

    /**
     * 异步调用 通过监听器触发回调 返回一个future 对象
     * @param request
     * @param callback
     */
    void invokeAsync(String address, Request request, Callback callback, long timeout);

    int getConnectionTimeout();

    void setConnectionTimeout(int timeout);

    boolean isShutdown();

    Map<String, Channel> channelTable();

}
