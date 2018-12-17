package com.gxl.Lighting.rpc.processor;

import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.Response;
import io.netty.channel.ChannelHandlerContext;

/**
 * 按照请求头 执行不同的处理逻辑
 */
public interface Processor {

    //todo 处理结果还没想好用什么类型
    void processRequest(ChannelHandlerContext ctx, Request request);

    void processResponse(ChannelHandlerContext ctx, Response response);
}
