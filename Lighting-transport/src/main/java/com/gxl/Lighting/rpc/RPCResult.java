package com.gxl.Lighting.rpc;

/**
 * 通信结果抽象
 */
public interface RPCResult {

    Object getResult();

    Throwable getCause();

    void setResult(Object result);

    void setCause(Throwable cause);
}
