package com.gxl.Lighting.rpc.processor;

import com.gxl.Lighting.rpc.Request;
import io.netty.channel.ChannelHandlerContext;

/**
 * 按照请求头 执行不同的处理逻辑
 */
public interface Processor {

    void processorRequest(ChannelHandlerContext ctx, Request request);
}
