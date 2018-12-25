package com.gxl.Lighting.netty.codec;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.*;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.enums.SerializationEnum;
import com.gxl.Lighting.netty.heartbeat.HeartBeat;
import com.gxl.Lighting.netty.param.*;
import com.gxl.Lighting.netty.serialization.Serialization;
import com.gxl.Lighting.netty.serialization.SerializationFactory;
import com.gxl.Lighting.util.BytesUtil;
import com.gxl.Lighting.util.StringUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.CodecException;
import io.netty.handler.codec.DecoderException;

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

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(LightingCodec.class);

    private static LightingCodec INSTANCE = new LightingCodec();

    /**serialize
     * 请求头长度
     */
    private static final int HEADER_LENGTH = 16;

    /**
     * 协议魔法数
     */
    private static final short MAGIC = (short)0xddff;

    private static final byte MAGIC_HIGH = BytesUtil.short2byte(MAGIC)[0];

    private static final byte MAGIC_LOW = BytesUtil.short2byte(MAGIC)[1];

    private static final byte JSON = (byte)1<<0;

    private static final byte HESSIAN = (byte)1<<1;

    private static final byte REQUEST = (byte)1<<2;

    private static final byte RESPONSE = (byte)1<<3;

    private static final byte SYNC = (byte)1<<4;

    private static final byte ASYNC = (byte)1<<5;

    private static final byte ONEWAY = (byte)1<<6;

    /**
     * 携带就代表是心跳包
     */
    private static final byte HEARTBEAT = (byte)1<<5;

    private LightingCodec(){ }

    public static LightingCodec getINSTANCE(){
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
     * @param data
     * @param byteBuf
     */
    public void encode(ChannelHandlerContext ctx, Object data, ByteBuf byteBuf) throws IOException{
        if(data instanceof Request) {
            final Request request = (Request) data;
            encodeRequest(ctx, request, byteBuf);
        } else if(data instanceof Response){
            final Response response = (Response) data;
            encodeResponse(ctx, response, byteBuf);
        } else if(data instanceof HeartBeat){
            final HeartBeat heartBeat = (HeartBeat)data;
            encodeHeartBeat(ctx, heartBeat, byteBuf);
        }

    }

    /**
     * 心跳包 只需要一个协议头就可以
     * @param ctx
     * @param heartBeat
     * @param byteBuf
     */
    private void encodeHeartBeat(ChannelHandlerContext ctx, HeartBeat heartBeat, ByteBuf byteBuf) {
        byte[] header = new byte[HEADER_LENGTH];
        //设置 魔法数
        BytesUtil.short2byte(MAGIC, header);
        //设置标识
        header[2] = HEARTBEAT;
        BytesUtil.int2byte(0, header, 12);
        byteBuf.writeBytes(header);
    }

    /**
     * 为 响应对象编码
     * @param ctx
     * @param response
     * @param byteBuf
     */
    private void encodeResponse(ChannelHandlerContext ctx, Response response, ByteBuf byteBuf) throws IOException{
        byte[] header = new byte[HEADER_LENGTH];
        //设置 魔法数
        BytesUtil.short2byte(MAGIC, header);
        //设置标识
        byte flag = getFlag(response);
        header[2] = flag;
        //响应对象不需要设置 Command
        header[3] = 0;
        BytesUtil.long2byte(response.getId(), header, 4);
        int saveIndex = byteBuf.writerIndex();
        byteBuf.writerIndex(saveIndex + HEADER_LENGTH);

        //将请求体进行序列化  得到长度后再设置到 header中
        Serialization serialization = SerializationFactory.newInstance(response.getSerialization());
        //创建输出流 方便序列化工具将 对象 写入
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        serialization.serialize(out, response.getResult());
        byte[] body = out.toByteArray();
        out.close();
        byteBuf.writeBytes(body);
        int bodySize = body.length;

        BytesUtil.int2byte(bodySize, header, 12);
        byteBuf.writerIndex(saveIndex);
        byteBuf.writeBytes(header);
        //指针归位
        byteBuf.writerIndex(saveIndex + HEADER_LENGTH + bodySize);
    }


    /**
     * 为 请求对象编码
     * @param ctx
     * @param request
     * @param byteBuf
     * @throws IOException
     */
    private void encodeRequest(ChannelHandlerContext ctx, Request request, ByteBuf byteBuf) throws IOException{
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
        //只需要序列化command  这样传输速度快
        serialization.serialize(out, request.getParam());
        byte[] body = out.toByteArray();
        out.close();
        byteBuf.writeBytes(body);
        int bodySize = byteBuf.readableBytes();

        BytesUtil.int2byte(bodySize, header, 12);
        byteBuf.writerIndex(saveIndex);
        byteBuf.writeBytes(header);
        //指针归位
        byteBuf.writerIndex(saveIndex + HEADER_LENGTH + bodySize);
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
            throw new IllegalArgumentException("request对象的序列化参数异常");
        }
        byte invoke = InvokeTypeEnum.getValue(request.getInvokeType());
        if(invoke == -1){
            throw new IllegalArgumentException("request对象的通信类型异常");
        }
        flag |= invoke;
        flag |= REQUEST;
        return flag;
    }

    /**
     * 从response 中 解析出 标识
     * @param response
     * @return
     */
    private byte getFlag(Response response) {
        String serialization = response.getSerialization();
        byte flag = SerializationEnum.getValue(serialization);
        if(flag == -1){
            throw new DecoderException("没有找到与"+serialization+"对应的序列化方式");
        }

        String invokeType = response.getInvokeType();
        byte invokeFlag = InvokeTypeEnum.getValue(invokeType);
        if(invokeFlag == -1){
            throw new DecoderException("没有找到与" + invokeType + "对应的invoke方式");
        }
        flag |= invokeFlag;
        flag |= RESPONSE;
        return flag;
    }

    /**
     * 解码核心逻辑
     * @param ctx
     * @param byteBuf
     * @param list
     */
    public void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list) throws IOException{
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
        if(byteBuf.readableBytes() < bodySize){
            //指针归位 保证下次解析正常
            byteBuf.readerIndex(saveIndex);
            return;
        }

        //先取出请求体
//        byteBuf.readerIndex(saveIndex + HEADER_LENGTH);
        byte[] body = new byte[bodySize];
        byteBuf.readBytes(body);

        Object result = null;
        if((header[2] & REQUEST) == REQUEST){
            result = decodeRequest(ctx, byteBuf, list, header, body);
        } else if((header[2] & RESPONSE) == RESPONSE){
            result = decodeResponse(ctx, byteBuf, list, header, body);
        } else if((header[2] & HEARTBEAT) == HEARTBEAT){
            result = HeartBeat.createHeartBeat();
        }
        list.add(result);
    }

    /**
     * 为响应对象解码
     * @param ctx
     * @param byteBuf
     * @param list
     * @param header
     * @param body
     */
    private Object decodeResponse(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list, byte[] header, byte[] body) throws IOException{
        String serializationType = decodeSerialization(header[2]);
        Serialization serialization = SerializationFactory.newInstance(serializationType);
        long id = decodeId(header);
        Result result = serialization.deserialize(new ByteArrayInputStream(body), body.length, Result.class);
        Response response = new Response(id);
        response.setResult(result);
        response.setSerialization(serializationType);
        return response;
    }

    private long decodeId(byte[] header) {
        return BytesUtil.bytes2long(header, 4);
    }


    /**
     * 从flag 中解析出response 的 相关信息
     * @return
     * @param flag
     */
    private String decodeSerialization(byte flag) {
        String serialization = SerializationEnum.getValue(flag);
        if(StringUtil.isEmpty(serialization)){
            throw new IllegalArgumentException("待解析数据的序列化标识异常");
        }
        return serialization;
    }

    private String decodeInvokeType(byte flag){
        String invokeType = InvokeTypeEnum.getValue(flag);
        if(StringUtil.isEmpty(invokeType)){
            throw new IllegalArgumentException("待解析数据的通信标识序列化异常");
        }
        return invokeType;
    }

    /**
     * 为请求对象解码
     * @param ctx
     * @param byteBuf
     * @param list
     * @param header
     * @param body
     */
    private Object decodeRequest(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> list, byte[] header, byte[] body) throws IOException {
        //根据标识创建Request 对象
        String serializationType = decodeSerialization(header[2]);
        Serialization serialization = SerializationFactory.newInstance(serializationType);
        CommandParam param = decodeCommand(header, body, serialization);
        String invokeType = decodeInvokeType(header[2]);
        Request request = Request.createRequest(RequestEnum.indexOf(header[3]), param, decodeId(header));
        request.setSerialization(serializationType);
        request.setInvokeType(invokeType);
        request.setParam(param);
        request.setCommand(header[3]);
        return request;
    }

    /**
     * 解析出命令方式
     * @param header
     * @param body
     * @param serialization
     * @return
     */
    private CommandParam decodeCommand(byte[] header, byte[] body, Serialization serialization) throws IOException{
        byte b = header[3];
        RequestEnum requestEnum =  RequestEnum.indexOf(b);
        CommandParam result = null;
        ByteArrayInputStream in = new ByteArrayInputStream(body);
        if(requestEnum == RequestEnum.INVOKE){
            result = serialization.deserialize(in, body.length, InvokeCommandParam.class);
        }
        if(requestEnum == RequestEnum.UNREGISTRY){
            result = serialization.deserialize(in, body.length, UnRegisterCommandParam.class);
        }
        if(requestEnum == RequestEnum.REGISTRY){
            result = serialization.deserialize(in, body.length, RegisterCommandParam.class);
        }
        if(requestEnum == RequestEnum.SUBSCRIBE){
            result = serialization.deserialize(in, body.length, SubscribeCommandParam.class);
        }
        if(requestEnum == RequestEnum.UNSUBSCRIBE){
            result = serialization.deserialize(in, body.length, UnSubscribeCommandParam.class);
        }
        if(requestEnum == RequestEnum.NOTIFY){
            result = serialization.deserialize(in, body.length, NotifyCommandParam.class);
        }
        if(requestEnum == RequestEnum.COLLECT){
            result = serialization.deserialize(in, body.length, CollectCommandParam.class);
        }
        in.close();
        return result;
    }

    public static byte getSYNC() {
        return SYNC;
    }

    public static byte getASYNC() {
        return ASYNC;
    }

    public static byte getONEWAY() {
        return ONEWAY;
    }
}
