package com.gxl.Lighting.rpc.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.Response;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import io.netty.channel.ChannelHandlerContext;

public class RegisterProcessor implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(RegisterProcessor.class);

    private final Registry registry;

    public RegisterProcessor(Registry registry){
        this.registry = registry;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        registry.registry((RegisterCommandParam)request.getParam());
    }

    public void processResponse(ChannelHandlerContext ctx, Response response) {

    }
}
