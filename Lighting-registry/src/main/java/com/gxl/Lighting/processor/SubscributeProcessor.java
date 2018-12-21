package com.gxl.Lighting.processor;

import com.gxl.Lighting.Registry;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.Response;
import com.gxl.Lighting.rpc.param.RegisterCommandParam;
import com.gxl.Lighting.rpc.param.SubscribeCommandParam;
import com.gxl.Lighting.rpc.processor.Processor;
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
        if(invokeType.equals(InvokeTypeEnum.ASYNC)) {
            registry.subscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSuccess(true);
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.SYNC)){
            registry.subscribe(param);
            //将结果写入对端
            Response response = new Response(request.getId());
            response.setSuccess(true);
            ctx.writeAndFlush(response);
        }
        if(invokeType.equals(InvokeTypeEnum.ONEWAY)){
            registry.subscribe(param);
        }
    }

}
