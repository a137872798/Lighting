package com.gxl.Lighting.netty.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class HessianSerialization implements Serialization{

    private HessianSerialization(){}

    private static volatile HessianSerialization INSTANCE;

    public static HessianSerialization getINSTANCE() {
        return INSTANCE;
    }

    public void serialize(OutputStream out, Object o) throws IOException {

    }

    public Object deserialize(InputStream in, int bodySize) throws IOException {
        return null;
    }
}
