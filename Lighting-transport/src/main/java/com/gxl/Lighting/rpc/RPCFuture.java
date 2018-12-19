package com.gxl.Lighting.rpc;

/**
 * 异步结果
 */
public interface RPCFuture {

    /**
     * 等待指定时间
     * @param millions
     * @return
     * @throws RemotingTimeoutException
     */
    RPCResult getResult(long millions) throws RemotingTimeoutException;

    RPCResult getResult();

    /**
     * 是否已经返回结果
     * @return
     */
    boolean hasResult();

    void setResult(RPCResult result);

    /**
     * 如果结果还没有返回 不阻塞当前线程
     * @return
     */
    RPCResult tryToGetResult();
}
