package com.gxl.Lighting.netty;

import com.gxl.Lighting.netty.heartbeat.HeartBeat;
import com.gxl.Lighting.netty.processor.ProcessorManager;
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
        } else if (msg instanceof HeartBeat){
            //noop 其实可以不用写这段 主要是 提醒自己已经 考虑到心跳包了
        }
        super.channelRead(ctx, msg);
    }
}
