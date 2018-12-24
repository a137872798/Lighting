package com.gxl.Lighting.netty.serialization;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HessianSerialization implements Serialization {

    private HessianSerialization() {
    }

    private static HessianSerialization INSTANCE = new HessianSerialization();

    public static HessianSerialization getINSTANCE() {
        return INSTANCE;
    }

    public void serialize(OutputStream out, Object o) throws IOException {
        HessianOutput hessianOutput = new HessianOutput(out);
        hessianOutput.writeObject(o);
        hessianOutput.close();
    }

    public <T> T deserialize(InputStream in, int bodySize, Class<T> type) throws IOException {
        HessianInput hessianInput = new HessianInput(in);
        return (T)hessianInput.readObject();
    }
}
