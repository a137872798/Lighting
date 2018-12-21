package com.gxl.Lighting.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.Response;
import com.gxl.Lighting.rpc.param.SubscribeCommandParam;
import com.gxl.Lighting.rpc.param.UnSubscribeCommandParam;
import com.gxl.Lighting.rpc.processor.Processor;
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
        if (invokeType.equals(InvokeTypeEnum.ASYNC)) {
            registry.unsubscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSuccess(true);
            ctx.writeAndFlush(response);
        }
        if (invokeType.equals(InvokeTypeEnum.SYNC)) {
            registry.unsubscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSuccess(true);
            ctx.writeAndFlush(response);
        }
        if (invokeType.equals(InvokeTypeEnum.ONEWAY)) {
            registry.unsubscribe(param);
        }
    }

}