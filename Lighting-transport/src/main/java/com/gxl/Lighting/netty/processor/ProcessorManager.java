package com.gxl.Lighting.netty.processor;

import com.gxl.Lighting.NamedThreadFactory;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.*;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 管理每个通信端的处理请求对象
 */
public class ProcessorManager implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ProcessorManager.class);

    private ConcurrentMap<RequestEnum, Processor> requestTable = new ConcurrentHashMap<RequestEnum, Processor>();

    /**
     * 处理请求的 线程池
     */
    private ExecutorService executorService = Executors.newFixedThreadPool(4, new NamedThreadFactory("processorManager.thread", true));

    /**
     * 保存了 所有待处理的 响应对象
     */
    private ConcurrentMap<Long, ResponseFuture> responseTable = new ConcurrentHashMap<Long, ResponseFuture>();

    public ProcessorManager(){}

    public ProcessorManager(ConcurrentMap<Long, ResponseFuture> responseTable) {
        this.responseTable = responseTable;
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
        final ChannelHandlerContext channelHandlerContext = ctx;
        final Request r = request;
        final Processor processor = this.requestTable.get(RequestEnum.indexOf(request.getCommand()));
        if(processor != null){
            Runnable run = new Runnable() {
                public void run() {
                    processor.processRequest(channelHandlerContext, r);
                }
            };
            //委托线程池 执行 操作 避免阻塞 独占线程
            logger.info("委托线程池处理" + r);
            executorService.execute(run);
        } else {
            logger.warn("找不到" + request.toString() + "对应的处理器对象");
        }
    }

    public void processResponse(ChannelHandlerContext ctx, Response response) {
        ResponseFuture future = this.responseTable.get(response.getId());
        //为null 就是被当作超时处理了
        if (future != null) {
            responseTable.remove(response.getId());
            if (future.getCallback() != null) {
                try {
                    future.getCallback().callback(future);
                } catch (RemotingException e) {
                    logger.error("{},{}",future.getResponse().getResult().getCause(), future.getResponse().getResult().getErrorMsg());
                }
            } else {
                future.setResponse(response);
            }
        }
    }

}
