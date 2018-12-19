package com.gxl.Lighting.netty.heartbeat;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.ClientMeta;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.rpc.Request;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;

/**
 * 配合 netty 的 IdleStateHandler 实现心跳检测
 */
@ChannelHandler.Sharable
public class HeartBeatHandler extends IdleStateHandler {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HeartBeatHandler.class);

    /**
     * client 端 长期没有触发 写事件 发送心跳包  server 长期没有触发 读事件 发送心跳包 他们的 触发点是不同的
     */
    private boolean isClient;

    /**
     * 需要该 服务器来断开失效的 client
     */
    private Server server;

    public HeartBeatHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, boolean isClient) {
        this(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, isClient, null);
    }
    public HeartBeatHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds, boolean isClient, Server server) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
        this.isClient = isClient;
        this.server = server;
    }

    /**
     * 实现心跳检测的 核心方法  该方法 通过检测 传入的 evt 来判断是需要发送哪种心跳事件
     * 这里做的是 单向的心跳检测 只需要客户端向服务器发心跳包 一旦 服务器 3次 触发超时时间 就 断开连接 同时客户端开始重连
     *
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        //代表服务器 很久没有读取到数据  因为是个 后台定时任务 只要客户端没有发送新的请求 lastReadTime 不会被更新 那么 就会继续触发该事件
        if(evt.state() == IdleState.READER_IDLE && !isClient){
            server.addHeartBeatTimes(ctx.channel());
        }
        if(evt.state() == IdleState.WRITER_IDLE && isClient){
            //因为 write 直接去了 上个节点 就无法触发本节点的 write 了 然后父类 在write 中 更新了 LastWriteTime 必须要触发这个
            //netty 的write 返回的 future 无法标记 当写入JDK channel 失败时 的状态  不能通过这种方式判断是否重连
            //直接在 客户端最外层进行重连就可以了
            ctx.pipeline().write(HeartBeat.createHeartBeat());
        }
        //All事件 不处理了 上面2种已经概括全部可能了
    }

    /**
     * 这里 应该要 清空服务器的  心跳包 超时次数
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        Map<Channel, ClientMeta> map = server.getClientMap();
        ClientMeta meta = map.get(ctx);
        if(meta == null){
            throw new NullPointerException("在服务器上找不到 地址为" + ctx.channel().remoteAddress() + "的客户端");
        }
        //读取成功重置
        meta.setHeartBeatTimes(0);

        super.channelReadComplete(ctx);
    }
}
