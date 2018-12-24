package com.gxl.Lighting.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.Request;
import com.gxl.Lighting.netty.Response;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.param.RegisterCommandParam;
import com.gxl.Lighting.netty.processor.Processor;
import io.netty.channel.ChannelHandlerContext;


public class RegisterProcessor implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(RegisterProcessor.class);

    private final Registry registry;

    public RegisterProcessor(Registry registry){
        this.registry = registry;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        String invokeType = request.getInvokeType();
        if(invokeType.equals(InvokeTypeEnum.ASYNC.getInvokeType())) {
            registry.register((RegisterCommandParam) request.getParam());
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.SYNC.getInvokeType())){
            registry.register((RegisterCommandParam) request.getParam());
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.ONEWAY.getInvokeType())){
            registry.register((RegisterCommandParam) request.getParam());
        }
    }

}
