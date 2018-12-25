package com.gxl.Lighting.netty;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.util.AddressUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 将 进行重连的 逻辑委托到这个handler对象
 */
public class ConnectionHandler extends ChannelInboundHandlerAdapter implements TimerTask {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(ConnectionHandler.class);

    /**
     * 记录重连的 地址
     */
    private final SocketAddress address;

    private final Timer timer;

    /**
     * 通过判断client 是否终止 确定是否要继续重连
     */
    private final DefaultClient client;

    /**
     * 重试次数
     */
    private int attempts;

    public ConnectionHandler(SocketAddress address, DefaultClient client, Timer timer) {
        this.address = address;
        this.client = client;
        this.timer = timer;
    }

    /**
     * 重新激活时 重试次数清空
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //通道重新激活时  重置 重试次数
        this.attempts = 0;
        super.channelActive(ctx);
    }

    /**
     * 检测到连接中断 开始 重连
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (!client.isShutdown()) {
            if (attempts < 5) {
                int timeout = attempts << 2;
                timer.newTimeout(this, timeout, TimeUnit.MILLISECONDS);
                logger.warn("与" + ctx.channel() + "断开连接， 地址是" + address + "开始重连");
            }
        }
        super.channelInactive(ctx);
    }


    public void run(final Timeout timeout) throws Exception {
        if(!client.isShutdown()) {
            synchronized (client.getBootstrap()) {
                if(!client.isShutdown() && !client.channelTable().get(AddressUtil.socketAddressToAddress((InetSocketAddress) address)).isActive()) {
                    final ChannelFuture future = client.getBootstrap().connect(address);

                    future.addListener(new ChannelFutureListener() {
                        public void operationComplete(ChannelFuture channelFuture) throws Exception {
                            boolean succeed = channelFuture.isSuccess();
                            logger.warn("重连到{}成功", address);
                            if (!succeed) {
                                logger.warn("重连到{}失败,短暂延时后会继续重连", address);

                                channelFuture.channel().pipeline().fireChannelInactive();
                            }
                        }
                    });
                }
            }
        }
    }
}
