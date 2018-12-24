package com.gxl.Lighting.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.Request;
import com.gxl.Lighting.netty.Response;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.param.UnSubscribeCommandParam;
import com.gxl.Lighting.netty.processor.Processor;
import com.gxl.Lighting.util.AddressUtil;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

public class UnSubscributeProcessor implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(UnSubscributeProcessor.class);

    private final Registry registry;

    public UnSubscributeProcessor(Registry registry) {
        this.registry = registry;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        String invokeType = request.getInvokeType();
        UnSubscribeCommandParam param = (UnSubscribeCommandParam) request.getParam();
        param.getMeta().setAddress(AddressUtil.socketAddressToAddress((InetSocketAddress) ctx.channel().remoteAddress()));
        if (invokeType.equals(InvokeTypeEnum.ASYNC.getInvokeType())) {
            registry.unsubscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if (invokeType.equals(InvokeTypeEnum.SYNC.getInvokeType())) {
            registry.unsubscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if (invokeType.equals(InvokeTypeEnum.ONEWAY.getInvokeType())) {
            registry.unsubscribe(param);
        }
    }

}