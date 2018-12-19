package com.gxl.Lighting.rpc;

import com.gxl.Lighting.rpc.processor.ProcessorManager;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 在解码后 分发请求的handler 对象
 */
@ChannelHandler.Sharable
public class DispatchHandler extends ChannelInboundHandlerAdapter {

    ProcessorManager processorManager;

    public DispatchHandler(ProcessorManager processorManager){
        this.processorManager = processorManager;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof Request){
            Request request = (Request)msg;
            processorManager.processRequest(ctx, request);
        } else if (msg instanceof Response){
            Response response = (Response)msg;
            processorManager.processResponse(ctx, response);
        }
        super.channelRead(ctx, msg);
    }
}
