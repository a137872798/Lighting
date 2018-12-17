package com.gxl.Lighting.netty;

/**
 * 远程通信接口
 */
public interface Remoting {

    void start();

    void shutdownGracefully();

    void shutdown();
}
