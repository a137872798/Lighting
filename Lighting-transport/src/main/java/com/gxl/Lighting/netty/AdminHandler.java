package com.gxl.Lighting.netty;

import com.gxl.Lighting.util.AddressUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 服务器上连接到所有客户端的长连接
 */
@ChannelHandler.Sharable
public class AdminHandler extends ChannelInboundHandlerAdapter{

    private final Map<Channel, ClientMeta> clientMap = new ConcurrentHashMap<Channel, ClientMeta>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //管理 客户端channel  因为这里是独占线程实现 所有不需要考虑线程安全问题
        if(clientMap.get(ctx.channel()) == null) {
            ClientMeta clientMeta = new ClientMeta();
            clientMeta.setHeartBeatTimes(DefaultServer.getDefaultHeartbeattimes());
            clientMeta.setAddress(AddressUtil.socketAddressToAddress((InetSocketAddress) ctx.channel().remoteAddress()));
            clientMap.put(ctx.channel(), clientMeta);
        }

        super.channelRead(ctx, msg);
    }

    public Map<Channel, ClientMeta> getClientMap() {
        return clientMap;
    }
}
