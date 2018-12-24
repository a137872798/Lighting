package com.gxl.Lighting.consumer.processor;

import com.gxl.Lighting.consumer.DefaultConsumer;
import com.gxl.Lighting.netty.Request;
import com.gxl.Lighting.netty.param.NotifyCommandParam;
import com.gxl.Lighting.netty.processor.Processor;
import io.netty.channel.ChannelHandlerContext;

/**
 * 处理 注册中心返回的  可使用服务提供者对象
 */
public class NotifyProcessor implements Processor {

    private DefaultConsumer consumer;

    public NotifyProcessor(DefaultConsumer consumer){
        this.consumer = consumer;
    }

    @Override
    public void processRequest(ChannelHandlerContext ctx, Request request) {
        NotifyCommandParam param = (NotifyCommandParam)request.getParam();
        consumer.notify(param.getServices());
    }
}
