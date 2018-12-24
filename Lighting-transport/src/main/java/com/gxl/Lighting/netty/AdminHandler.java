package com.gxl.Lighting.netty;

import com.gxl.Lighting.util.AddressUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理 服务器上连接到所有客户端的长连接
 */
@ChannelHandler.Sharable
public class AdminHandler extends ChannelInboundHandlerAdapter{

    private final Map<String, ClientMeta> clientMap = new ConcurrentHashMap<String, ClientMeta>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //这个ctx是NioserverSocketChannel 的 pipeline 所以是没有remoteAddress的

        //管理 客户端channel  因为这里是独占线程实现 所有不需要考虑线程安全问题
        if(clientMap.get(ctx.channel()) == null) {
            ClientMeta clientMeta = new ClientMeta();
            clientMeta.setHeartBeatTimes(DefaultServer.getDefaultHeartbeattimes());
            NioSocketChannel channel =  (NioSocketChannel)msg;
            String address = AddressUtil.socketAddressToAddress(channel.remoteAddress());
            clientMeta.setAddress(address);
            clientMeta.setChannel(channel);
            clientMap.put(address, clientMeta);
        }

        super.channelRead(ctx, msg);
    }

    public Map<String, ClientMeta> getClientMap() {
        return clientMap;
    }
}
