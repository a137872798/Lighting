package com.gxl.Lighting;

import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.UnpooledDirectByteBuf;

public class BytebufTest {
    public static void main(String[] args) {
        UnpooledDirectByteBuf byteBuf = (UnpooledDirectByteBuf) ByteBufAllocator.DEFAULT.directBuffer();
        byteBuf.writeBytes(new byte[]{1,2,3,4,43});
        System.out.println(byteBuf.readerIndex());
        byteBuf.getBytes(0, new byte[10]);
        System.out.println(byteBuf.readerIndex());
    }
}
