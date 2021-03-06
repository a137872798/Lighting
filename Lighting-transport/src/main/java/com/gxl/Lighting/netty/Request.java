package com.gxl.Lighting.netty;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * 请求的 载体对象
 */
public class Request {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Request.class);
    /**
     * 为每个请求 生成唯一id
     */
    private static final AtomicLong ID_GENERITOR = new AtomicLong(0);

    /**
     * 所有请求的上级接口
     */
    private CommandParam param;

    /**
     * 请求类型
     */
    private int command;

    /**
     * 序列化方式
     */
    private String serialization;

    /**
     * 请求方式 对端需要依靠这个标识来确定是否需要返回结果
     */
    private String invokeType;

    private long id;

    private Request(int command, CommandParam param, long id) {
        if (id != -1) {
            this.id = id;
        } else {
            long tempId = ID_GENERITOR.incrementAndGet();
            //如果出现越界  重置id
            if (tempId == Long.MIN_VALUE) {
                logger.debug("request id 出现了 Long.MIN_VALUE 现在被重置为0");
                tempId = 0;
                this.id = tempId;
            }
            this.command = command;
            this.param = param;
        }
    }

    public static Request createRequest(RequestEnum type, CommandParam param){
        return createRequest(type, param, -1);
    }

    public static Request createRequest(RequestEnum type, CommandParam param, long id) {
        return new Request(type.ordinal(), param, id);
    }

    public CommandParam getParam() {
        return param;
    }

    public void setParam(CommandParam param) {
        this.param = param;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public String getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(String invokeType) {
        this.invokeType = invokeType;
    }

    @Override
    public String toString() {
        return "Request{" + "param=" + param + ", command=" + command + ", serialization='" + serialization + '\'' + ", invokeType='" + invokeType + '\'' + ", id=" + id + '}';
    }


}
