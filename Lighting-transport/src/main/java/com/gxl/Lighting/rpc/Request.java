package com.gxl.Lighting.rpc;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.sun.xml.internal.bind.v2.model.core.ID;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 请求的 载体对象
 */
public class Request {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Request.class);
    /**
     * 为每个请求 生成唯一id
     */
    private static final AtomicInteger ID_GENERITOR = new AtomicInteger(0);

    /**
     * 如果是 RPC 请求 需要传入参数
     */
    private RPCParam rpcParam;

    private int id;

    /**
     * 是否是心跳检测
     */
    private boolean isHeartBeat;

    public Request(){
        isHeartBeat = false;
        int tempId = ID_GENERITOR.incrementAndGet();
        //如果出现越界  重置id
        if(tempId == Integer.MIN_VALUE){
            logger.debug("request id 出现了 Integer.MIN_VALUE 现在被重置为0");
            tempId = 0;
        }
        id = tempId;
    }

    public Request(boolean isHeartBeat){
        this.isHeartBeat = isHeartBeat;
        int tempId = ID_GENERITOR.incrementAndGet();
        //如果出现越界  重置id
        if(tempId == Integer.MIN_VALUE){
            logger.debug("request id 出现了 Integer.MIN_VALUE 现在被重置为0");
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isHeartBeat() {
        return isHeartBeat;
    }

    public void setHeartBeat(boolean heartBeat) {
        isHeartBeat = heartBeat;
    }
}
