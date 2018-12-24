package com.gxl.Lighting.monitor.processor;

import com.gxl.Lighting.monitor.DefaultMonitor;
import com.gxl.Lighting.monitor.Monitor;
import com.gxl.Lighting.netty.Request;
import com.gxl.Lighting.netty.param.CollectCommandParam;
import com.gxl.Lighting.netty.processor.Processor;
import io.netty.channel.ChannelHandlerContext;

public class CollectProcessor implements Processor {

    private Monitor monitor;

    public CollectProcessor(DefaultMonitor monitor) {
        this.monitor = monitor;
    }

    public void processRequest(ChannelHandlerContext ctx, Request request) {
        monitor.collect((CollectCommandParam) request.getParam());
    }
}
