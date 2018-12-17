package com.gxl.Lighting.netty.serialization;

import com.alibaba.fastjson.JSON;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class JsonSerialization implements Serialization{

    private JsonSerialization(){}

    private static volatile JsonSerialization INSTANCE;

    public static JsonSerialization getINSTANCE() {
        return INSTANCE;
    }

    public void serialize(OutputStream out, Object o) throws IOException{
        String str = JSON.toJSONString(o);
        out.write(str.getBytes("UTF-8"));
    }

    public Object deserialize(InputStream in, int bodySize) throws IOException{
        byte[] body = new byte[bodySize];
        in.read(body);
        return JSON.parse(new String(body));
    }
}
