package com.gxl.Lighting.netty;

/**
 * 响应结果对象
 * 序列化流程是 生成请求头的时候设置了 根据Response/Request 的 序列化类型 设置flag 然后数据体本身被序列化
 * 然后对端 通过 请求头中的 flag 进行反序列化
 */
public class Response {

    private Result result = new Result();

    /**
     * 对应的请求id 通过 这个和responseFuture 的 id 对应 才能知道要设置到哪个 future 中
     */
    private long id;

    /**
     * result的序列化方式
     */
    private String serialization;

    private String invokeType;

    public Response(long id) {
        this.id = id;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public Result getResult() {
        return result;
    }

    public String getInvokeType() {
        return invokeType;
    }

    public void setInvokeType(String invokeType) {
        this.invokeType = invokeType;
    }

    public void setResult(Result result) {
        this.result = result;
    }
}


