package com.gxl.Lighting.netty.serialization;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.param.RegisterCommandParam;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FastJsonSerialization implements Serialization{

    private FastJsonSerialization(){}

    private static final InternalLogger  logger = InternalLoggerFactory.getInstance(FastJsonSerialization.class);

    private static volatile FastJsonSerialization INSTANCE = new FastJsonSerialization();

    public static FastJsonSerialization getINSTANCE() {
        return INSTANCE;
    }

    public void serialize(OutputStream out, Object o) throws IOException{
        String str = JSON.toJSONString(o);
        logger.debug("序列化后的json是" + str);
        out.write(str.getBytes("UTF-8"));
    }

    public <T> T deserialize(InputStream in, int bodySize, Class<T> type) throws IOException{
        byte[] body = new byte[bodySize];
        in.read(body);
        String json = new String(body, "UTF-8");
        logger.debug("反序列化的json是" + json);
        return JSON.parseObject(json.trim(), type);
    }
}
