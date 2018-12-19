package com.gxl.Lighting.netty;

import io.netty.channel.Channel;

import java.util.Map;

/**
 * RPC 通信的 服务端顶层接口
 */
public interface Server extends Remoting{

    /**
     * 服务器 增加 心跳失败的 次数 因为 客户端会定期发送心跳包 没有收到 就代表 该channel 失效了
     */
    void addHeartBeatTimes(Channel channel);

    /**
     * 获取 该服务器维护的 所有客户端channel
     * @return
     */
    Map<Channel, ClientMeta> getClientMap();
}
