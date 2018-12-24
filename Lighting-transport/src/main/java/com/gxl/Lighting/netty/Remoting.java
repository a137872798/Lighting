package com.gxl.Lighting.netty;

import com.gxl.Lighting.netty.processor.ProcessorManager;

/**
 * 远程通信接口
 */
public interface Remoting {

    void start();

    void shutdownGracefully();

    ProcessorManager getProcessorManager();

    void setProcessorManager(ProcessorManager manager);
}
