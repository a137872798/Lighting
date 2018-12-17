package com.gxl.Lighting.netty.enums;

import com.gxl.Lighting.netty.codec.LightingCodec;

public enum SerializationEnum {

    JSON(LightingCodec.getJSON(),"json"),
    HESSIAN(LightingCodec.getHESSIAN(),"hessian");


    private byte flag;

    private String serialization;

    SerializationEnum(byte flag, String serialization){
        this.flag = flag;
        this.serialization = serialization;
    }

    public byte getFlag() {
        return flag;
    }

    public String getSerialization() {
        return serialization;
    }

    public static byte getValue(String serialization){
        for(SerializationEnum temp : SerializationEnum.values()){
            if(temp.getSerialization().equalsIgnoreCase(serialization)){
                return temp.getFlag();
            }
        }
        return (byte)-1;
    }

    public static String getValue(byte b){
        for(SerializationEnum temp : SerializationEnum.values()){
            if(temp.getFlag() == b){
                return temp.getSerialization();
            }
        }
        return null;
    }
}
