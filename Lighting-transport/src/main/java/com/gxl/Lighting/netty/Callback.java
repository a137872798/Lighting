package com.gxl.Lighting.netty;

/**
 * 异步调用的 回调函数
 */
public interface Callback {

    void callback(ResponseFuture future) throws RemotingException;
}
