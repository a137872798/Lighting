package com.gxl.Lighting.provider.processor;

import com.gxl.Lighting.netty.Request;
import com.gxl.Lighting.netty.Response;
import com.gxl.Lighting.netty.Result;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.param.InvokeCommandParam;
import com.gxl.Lighting.provider.DefaultProvider;
import com.gxl.Lighting.proxy.Invoker;

import com.gxl.Lighting.netty.processor.Processor;
import io.netty.channel.ChannelHandlerContext;

public class InvokerProcessor implements Processor {

    private DefaultProvider provider;

    public InvokerProcessor(DefaultProvider provider) {
        this.provider = provider;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        Invoker invoker = provider.getInvoker();
        Object  result = null;
        Throwable t = null;
        try {
            result = invoker.invoke((InvokeCommandParam)request.getParam());
        } catch (Exception e) {
            t = e;
        }
        Response response = new Response(request.getId());
        response.setSerialization(request.getSerialization());
        response.setInvokeType(InvokeTypeEnum.ONEWAY.getInvokeType());
        Result r = new Result();
        if(result == null){
            r.setSuccess(false);
            r.setCause(t);
            r.setErrorMsg(t.getMessage());
            r.setResult(null);
            response.setResult(r);
        }else {
            r.setResult(result);
            r.setSuccess(true);
            response.setResult(r);
        }
        ctx.writeAndFlush(response);
    }
}
