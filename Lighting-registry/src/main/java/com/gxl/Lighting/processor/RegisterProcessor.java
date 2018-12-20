package com.gxl.Lighting.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.Response;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.processor.Processor;
import io.netty.channel.ChannelHandlerContext;


public class RegisterProcessor implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(RegisterProcessor.class);

    private final Registry registry;

    public RegisterProcessor(Registry registry){
        this.registry = registry;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        String invokeType = request.getInvokeType();
        if(invokeType.equals(InvokeTypeEnum.ASYNC)) {
            registry.register((RegisterCommandParam) request.getParam());
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSuccess(true);
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.SYNC)){
            registry.register((RegisterCommandParam) request.getParam());
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSuccess(true);
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.ONEWAY)){
            registry.register((RegisterCommandParam) request.getParam());
        }
    }

}
