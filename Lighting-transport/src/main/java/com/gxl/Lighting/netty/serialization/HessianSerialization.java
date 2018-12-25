package com.gxl.Lighting.netty.serialization;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HessianSerialization implements Serialization {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HessianSerialization.class);

    private HessianSerialization() {
    }

    private static HessianSerialization INSTANCE = new HessianSerialization();

    public static HessianSerialization getINSTANCE() {
        return INSTANCE;
    }

    public void serialize(OutputStream out, Object o) throws IOException {
        HessianOutput hessianOutput = new HessianOutput(out);
        hessianOutput.writeObject(o);
        logger.info("使用hessian进行序列化");
        hessianOutput.close();
    }

    public <T> T deserialize(InputStream in, int bodySize, Class<T> type) throws IOException {
        HessianInput hessianInput = new HessianInput(in);
        logger.info("使用hessian进行反序列化");
        return (T)hessianInput.readObject();
    }
}
