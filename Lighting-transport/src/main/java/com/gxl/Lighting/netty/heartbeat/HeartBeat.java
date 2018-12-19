package com.gxl.Lighting.netty.heartbeat;

/**
 * 心跳包对象 不需要任何内容只要再 协议中添加 心跳包标识就可以
 */
public class HeartBeat {

    private static final HeartBeat INSTANCE = new HeartBeat();

    private HeartBeat(){}

    public static HeartBeat createHeartBeat(){ return INSTANCE;}
}
