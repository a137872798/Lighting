package com.gxl.Lighting.rpc.processor;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.RequestEnum;
import com.gxl.Lighting.rpc.Response;
import com.gxl.Lighting.rpc.ResponseFuture;
import io.netty.channel.ChannelHandlerContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 管理每个通信端的处理请求对象
 */
public class ProcessorManager implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ProcessorManager.class);

    private ConcurrentMap<RequestEnum, Processor> requestTable = new ConcurrentHashMap<RequestEnum, Processor>();

    /**
     * 保存了 所有待处理的 响应对象
     */
    private ConcurrentMap<Long, ResponseFuture> responseTable = new ConcurrentHashMap<Long, ResponseFuture>();

    public ProcessorManager(ConcurrentMap<Long, ResponseFuture> responseTable) {
        this.requestTable = requestTable;
    }

    public boolean registerProcessor(RequestEnum requestEnum, Processor processor) {
        if (requestEnum == null || processor == null) {
            return false;
        }
        Processor old = this.requestTable.putIfAbsent(requestEnum, processor);
        if (old != null) {
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

    public void removeProcessor(RequestEnum requestEnum) {
        this.requestTable.remove(requestEnum);
    }


    public void processRequest(ChannelHandlerContext ctx, Request request) {
        this.requestTable.get(request.getCommand()).processRequest(ctx, request);
    }

    public void processResponse(ChannelHandlerContext ctx, Response response) {
        ResponseFuture future = this.responseTable.get(response.getId());
        //为null 就是被当作超时处理了
        if (future != null) {
            responseTable.remove(response.getId());
            if (future.getCallback() != null) {
                future.getCallback().callback(response);
            } else {
                future.setResponse(response);
            }
        }
    }

}
