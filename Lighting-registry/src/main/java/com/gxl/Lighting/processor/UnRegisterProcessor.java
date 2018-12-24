package com.gxl.Lighting.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.Request;
import com.gxl.Lighting.netty.Response;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.param.UnRegisterCommandParam;
import com.gxl.Lighting.netty.processor.Processor;
import io.netty.channel.ChannelHandlerContext;

public class UnRegisterProcessor implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnRegisterProcessor.class);

    private final Registry registry;

    public UnRegisterProcessor(Registry registry){
        this.registry = registry;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        String invokeType = request.getInvokeType();
        if(invokeType.equals(InvokeTypeEnum.ASYNC.getInvokeType())) {
            registry.unregister((UnRegisterCommandParam) request.getParam());
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.SYNC.getInvokeType())){
            registry.unregister((UnRegisterCommandParam) request.getParam());
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.ONEWAY.getInvokeType())){
            registry.unregister((UnRegisterCommandParam) request.getParam());
        }
    }
}
