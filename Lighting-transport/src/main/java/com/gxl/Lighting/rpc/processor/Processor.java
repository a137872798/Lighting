package com.gxl.Lighting.rpc.processor;

import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.Response;
import io.netty.channel.ChannelHandlerContext;

/**
 * 按照请求头 执行不同的处理逻辑
 */
public interface Processor {

    //实际处理的逻辑要转发 到 对应的 角色中
    void processRequest(ChannelHandlerContext ctx, Request request);
}
