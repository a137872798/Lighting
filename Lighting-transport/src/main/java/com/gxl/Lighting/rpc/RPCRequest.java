package com.gxl.Lighting.rpc;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求的 载体对象
 */
public class RPCRequest {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(RPCRequest.class);
    /**
     * 为每个请求 生成唯一id
     */
    private static final AtomicLong ID_GENERITOR = new AtomicLong(0);

    /**
     * 如果是 RPC 请求 需要传入参数
     */
    private RPCParam rpcParam;

    /**
     * 序列化方式
     */
    private String serialization;

    private long id;

    /**
     * 是否是心跳检测
     */
    private boolean isHeartBeat;

    private String invokeWay;

    public RPCRequest(){
        isHeartBeat = false;
        long tempId = ID_GENERITOR.incrementAndGet();
        //如果出现越界  重置id
        if(tempId == Long.MIN_VALUE){
            logger.debug("request id 出现了 Long.MIN_VALUE 现在被重置为0");
            tempId = 0;
        }
        this.id = tempId;
    }

    public RPCRequest(int id){
        isHeartBeat = false;
        if(id == Long.MIN_VALUE){
            logger.debug("request id 出现了 Long.MIN_VALUE 现在被重置为0");
            id = 0;
        }
        this.id = id;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public RPCRequest(boolean isHeartBeat){
        this.isHeartBeat = isHeartBeat;
        long tempId = ID_GENERITOR.incrementAndGet();
        //如果出现越界  重置id
        if(tempId == Integer.MIN_VALUE){
            logger.debug("request id 出现了 Long.MIN_VALUE 现在被重置为0");
            tempId = 0;
        }
        id = tempId;
    }

    public RPCParam getRpcParam() {
        return rpcParam;
    }

    public void setRpcParam(RPCParam rpcParam) {
        this.rpcParam = rpcParam;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isHeartBeat() {
        return isHeartBeat;
    }

    public String getInvokeWay() {
        return invokeWay;
    }

    public void setInvokeWay(String invokeWay) {
        this.invokeWay = invokeWay;
    }

    public void setHeartBeat(boolean heartBeat) {
        isHeartBeat = heartBeat;
    }
}
