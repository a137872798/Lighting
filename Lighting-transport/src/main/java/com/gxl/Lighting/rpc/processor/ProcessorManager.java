package com.gxl.Lighting.rpc.processor;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.RequestEnum;
import com.gxl.Lighting.rpc.Response;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理每个通信端的处理请求对象
 */
public class ProcessorManager implements Processor{

    //TODO response 可能不需要处理
    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ProcessorManager.class);

    private ConcurrentMap<RequestEnum, Processor> requestTable = new ConcurrentHashMap<RequestEnum, Processor>();

    private ConcurrentMap<Respons>

    public boolean registerProcessor(RequestEnum requestEnum, Processor processor){
        if(requestEnum == null || processor == null){
            return false;
        }
        Processor old = this.requestTable.putIfAbsent(requestEnum, processor);
        if(old != null){
            logger.debug("已经存在" + requestEnum.name() + "类型的processor了");
            return false;
        }
        return true;
    }

    public ConcurrentMap<RequestEnum, Processor> getRequestTable() {
        return requestTable;
    }

    public void setRequestTable(ConcurrentMap<RequestEnum, Processor> requestTable) {
        this.requestTable = requestTable;
    }

    public void removeProcessor(RequestEnum requestEnum){
        this.requestTable.remove(requestEnum);
    }


    public void processRequest(ChannelHandlerContext ctx, Request request) {
        this.requestTable.get(request).processRequest(ctx, request);
    }

    public void processResponse(ChannelHandlerContext ctx, Response response) {

    }
}
