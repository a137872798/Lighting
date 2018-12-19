package com.gxl.Lighting.rpc;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 这个类是客户端用来 接受 对端发来的 response 对象的
 */
public class ResponseFuture {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ResponseFuture.class);

    /**
     * 同步调用时 设置服务端 返回的结果
     */
    private Response response;

    /**
     * 调用的超时时间
     */
    private long timeout;

    /**
     * 收到服务端 结果时 触发的回调函数
     */
    private Callback callback;

    /**
     * 标识 这个 future 是对应哪个请求的
     */
    private long id;

    /**
     * 是否成功发送到对端
     */
    private boolean sendSuccess;

    /**
     * 阻塞线程的对象
     */
    private CountDownLatch latch = new CountDownLatch(1);

    /**
     * 该future 失效的 异常原因
     */
    private Throwable cause;

    public ResponseFuture(long id, Callback callback,long timeout){
        this.id = id;
        this.callback = callback;
        this.timeout = timeout;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
        latch.countDown();
    }

    public Callback getCallback() {
        return callback;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isSendSuccess() {
        return sendSuccess;
    }

    public void setSendSuccess(boolean sendSuccess) {
        this.sendSuccess = sendSuccess;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public Throwable getCause() {
        return cause;
    }

    public void setCause(Throwable cause) {
        this.cause = cause;
    }

    /**
     * 等待 响应  这里出现异常 或者设置了 response 都会 唤醒
     * @param timeout
     */
    public Response waitResponseUnInterrupt(long timeout, TimeUnit unit) {
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            //noop
        }
        return response;
    }


}