package com.gxl.Lighting.netty;

import com.gxl.Lighting.rpc.processor.ProcessorManager;

/**
 * 远程通信接口
 */
public interface Remoting {

    void start();

    void shutdownGracefully();

    void shutdown();

    ProcessorManager getProcessorManager();

    void setProcessorManager(ProcessorManager manager);
}
