package com.gxl.Lighting.rpc.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.Response;
import com.gxl.Lighting.rpc.param.UnSubscributeCommandParam;
import io.netty.channel.ChannelHandlerContext;

public class UnSubscributeProcessor implements Processor{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnSubscributeProcessor.class);

    private final Registry registry;

    public UnSubscributeProcessor(Registry registry){
        this.registry = registry;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        registry.unsubscribute((UnSubscributeCommandParam) request.getParam());
    }

    public void processResponse(ChannelHandlerContext ctx, Response response) {

    }
}
