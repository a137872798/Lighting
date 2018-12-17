package com.gxl.Lighting.rpc;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 在解码后 分发请求的handler 对象
 */
@ChannelHandler.Sharable
public class DispatchHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //TODO 这里要根据请求头 类型 执行不同的操作
        if(msg instanceof Request){
            Request request = (Request)msg;
            processorRequest(request);
        }
        super.channelRead(ctx, msg);
    }

    private static class Processor{
        private Map<RequestEnum, Processor>
    }
}
