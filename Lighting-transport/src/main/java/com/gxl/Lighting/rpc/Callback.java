package com.gxl.Lighting.rpc;

/**
 * 异步调用的 回调函数
 */
public interface Callback {

    void callback(ResponseFuture future);
}
