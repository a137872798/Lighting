package com.gxl.Lighting.netty.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Lighting 自带的编码器
 */
@ChannelHandler.Sharable
public class LightingEncoder extends MessageToByteEncoder {
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        LightingCodec.getINSTANCE().encode(channelHandlerContext, o, byteBuf);
    }
}
