package com.gxl.Lighting.netty.codec;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.enums.InvokeWayEnum;
import com.gxl.Lighting.netty.enums.SerializationEnum;
import com.gxl.Lighting.netty.serialization.Serialization;
import com.gxl.Lighting.netty.serialization.SerializationFactory;
import com.gxl.Lighting.rpc.Request;
import com.gxl.Lighting.rpc.RequestEnum;
import com.gxl.Lighting.util.BytesUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * 管理通信协议的对象
 * FLAG 是 请求方式 和 序列化方式 LENGTH 是 消息体长度
 * |------------------------------------------------------------------------------------------|
 * |MAGIC    2byte|FLAG      1byte|Command       1byte|ID        8byte|LENGTH          4byte |
 * |----------------------------------------------------------------------------------------|
 */
public class LightingCodec {


    //TODO 好像写了关于 Request 的 编解码 没有 Response的 可以考虑加在 flag 上


    private static final InternalLogger logger = InternalLoggerFactory.getInstance(LightingCodec.class);

    private static volatile LightingCodec INSTANCE;

    /**
     * 请求头长度
     */
    private static final int HEADER_LENGTH = 16;

    /**
     * 协议魔法数
     */
    private static final short MAGIC = (short)0xddff;

    private static final byte MAGIC_HIGH = BytesUtil.short2byte(MAGIC)[0];

    private static final byte MAGIC_LOW = BytesUtil.short2byte(MAGIC)[1];


    private static final byte ONEWAY = (byte)1;

    private static final byte SYNC = (byte)1<<1;

    private static final byte ASYNC = (byte)1<<2;

    private static final byte JSON = (byte)1<<3;

    private static final byte HESSIAN = (byte)1<<4;


    private LightingCodec(){ }

    public static LightingCodec getINSTANCE(){
        if(INSTANCE == null){
            INSTANCE = new LightingCodec();
        }
        return INSTANCE;
    }

    public static int getHeaderLength() {
        return HEADER_LENGTH;
    }

    public static short getMAGIC() {
        return MAGIC;
    }

    /**
     * 编码
     * @param ctx
     * @param request
     * @param byteBuf
     */
    public void encode(ChannelHandlerContext ctx, Request request, ByteBuf byteBuf) throws IOException{
        byte[] header = new byte[HEADER_LENGTH];
        //设置 魔法数
        BytesUtil.short2byte(MAGIC, header);
        //设置标识
        byte flag = getFlag(request);
        header[2] = flag;
        header[3] = (byte) request.getCommand();
        BytesUtil.long2byte(request.getId(), header, 4);

        int saveIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(saveIndex + HEADER_LENGTH);

        //将请求体进行序列化  得到长度后再设置到 header中
        Serialization serialization = SerializationFactory.newInstance(request.getSerialization());
        //创建输出流 方便序列化工具将 对象 写入
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialization.serialize(out, request.getParam());
        byte[] body = out.toByteArray();
        byteBuf.writeBytes(body);
        int bodySize = byteBuf.readableBytes();

        BytesUtil.int2byte(bodySize, header, 12);
        byteBuf.writerIndex(saveIndex);
        byteBuf.writeBytes(header);
        //指针归位
        byteBuf.writerIndex(saveIndex + HEADER_LENGTH + bodySize);
    }

    public static byte getONEWAY() {
        return ONEWAY;
    }

    public static byte getSYNC() {
        return SYNC;
    }

    public static byte getASYNC() {
        return ASYNC;
    }

    public static byte getJSON() {
        return JSON;
    }

    public static byte getHESSIAN() {
        return HESSIAN;
    }

    /**
     * 从request 中 解析出 标识
     * @param request
     * @return
     */
    private byte getFlag(Request request) {
        String serialization = request.getSerialization();
        byte flag = SerializationEnum.getValue(serialization);
        if(flag == -1){
            logger.warn("没有找到与"+serialization+"对应的序列化方式, 默认使用json 进行序列化");
            flag = LightingCodec.getJSON();
        }

        String invokeWay = request.getInvokeWay();
        byte temp = InvokeWayEnum.getValue(invokeWay);
        if(temp == -1){
            logger.warn("没有找到与" + invokeWay + "对应的请求方式, 默认使用SYNC请求");
            flag |= SYNC;
        } else {
            flag |= temp;
        }
        return flag;
    }

    /**
     * 解码核心逻辑
     * @param channelHandlerContext
     * @param byteBuf
     * @param list
     */
    public void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws IOException{
        //长度小于一个 协议头 直接放弃
        if(byteBuf.readableBytes() < HEADER_LENGTH){
            return;
        }
        int saveIndex = byteBuf.readerIndex();

        byte[] header = new byte[HEADER_LENGTH];
        byteBuf.readBytes(header);
        if(header[0] != MAGIC_HIGH || header[1] != MAGIC_LOW){
            logger.error("接受到的消息魔法数错误");
            throw new CodecException("接受到的消息魔法数错误");
        }
        int bodySize = BytesUtil.bytes2int(header, 12);
        //本次长度 不足以完全解析一个数据包
        if(byteBuf.readableBytes() < HEADER_LENGTH + bodySize){
            //指针归位 保证下次解析正常
            byteBuf.readerIndex(saveIndex);
            return;
        }

        //根据标识创建Request 对象
        Request request = decodeFlag(header[2],(int)header[3]);
        request.setCommand(header[3]);
        request.setId(BytesUtil.bytes2long(header, 4));
        byteBuf.readerIndex(saveIndex + HEADER_LENGTH);
        byte[] body = new byte[bodySize];
        byteBuf.readBytes(body);
        Serialization serialization = SerializationFactory.newInstance(request.getSerialization());
        Object result = serialization.deserialize(new ByteArrayInputStream(body), bodySize);
        if(!(result instanceof Request)){
            logger.error("从RPCRequest对象中 没有获取到RPCParam 对象");
            throw new CodecException("从RPCRequest对象中 没有获取到RPCParam 对象");
        }
        list.add(result);
        byteBuf.readerIndex(saveIndex + HEADER_LENGTH + bodySize);
    }

    /**
     * 解析标识并生成对应的 request 对象
     * @param flag
     * @return
     */
    private Request decodeFlag(byte flag, int commadType) {
        String serialization = null;
        String invokeWay = null;
        if((flag & ONEWAY) == 1){
            invokeWay = InvokeWayEnum.getValue(ONEWAY);
        }
        if((flag & SYNC) == 1){
            invokeWay = InvokeWayEnum.getValue(SYNC);
        }
        if((flag & ASYNC) == 1){
            invokeWay = InvokeWayEnum.getValue(ASYNC);
        }
        if((flag & JSON) == 1){
            serialization = SerializationEnum.getValue(JSON);
        }
        if((flag & HESSIAN) == 1){
            serialization = SerializationEnum.getValue(HESSIAN);
        }

        RequestEnum type = RequestEnum.indexOf(commadType);
        if(type == null){
            logger.error("请求对象的 命令类型不存在");
            throw new CodecException("请求对象的 命令类型不存在");
        }
        Request request = Request.createRequest(type, null);
        request.setInvokeWay(invokeWay);
        request.setSerialization(serialization);
        return request;
    }
}
