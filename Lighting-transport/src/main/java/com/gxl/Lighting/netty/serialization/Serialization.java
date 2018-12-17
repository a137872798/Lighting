package com.gxl.Lighting.netty.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化上级接口
 */
public interface Serialization {

    /**
     * 将序列化结果设置到 输出流上
     * @param out
     * @param o
     */
    void serialize(OutputStream out, Object o) throws IOException;

    /**
     * 从输入流中 解析对象
     * @param in
     * @return
     */
    Object deserialize(InputStream in, int bodySize) throws IOException;
}

