package com.gxl.Lighting.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.Request;
import com.gxl.Lighting.netty.Response;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.param.SubscribeCommandParam;
import com.gxl.Lighting.netty.processor.Processor;
import com.gxl.Lighting.util.AddressUtil;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;


public class SubscributeProcessor implements Processor {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SubscributeProcessor.class);

    private final Registry registry;

    public SubscributeProcessor(Registry registry){
        this.registry = registry;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        String invokeType = request.getInvokeType();
        SubscribeCommandParam param = (SubscribeCommandParam) request.getParam();
        param.getMeta().setAddress(AddressUtil.socketAddressToAddress((InetSocketAddress) ctx.channel().remoteAddress()));
        if(invokeType.equals(InvokeTypeEnum.ASYNC.getInvokeType())) {
            registry.subscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.SYNC.getInvokeType())){
            registry.subscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSerialization(request.getSerialization());
            response.getResult().setSuccess(true);
            response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.ONEWAY.getInvokeType())){
            registry.subscribe(param);
        }
    }

}
