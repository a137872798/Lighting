package com.gxl.Lighting.netty;

import io.netty.channel.Channel;

/**
 * 保存客户端 元素据
 */
public class ClientMeta {
    private String address;

    /**
     * 心跳丢失次数
     */
    private int heartBeatTimes;

    private Channel channel;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getHeartBeatTimes() {
        return heartBeatTimes;
    }

    public void addHeartBeatTimes(){
        this.heartBeatTimes++;
    }

    public void setHeartBeatTimes(int heartBeatTimes) {
        this.heartBeatTimes = heartBeatTimes;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }
}
