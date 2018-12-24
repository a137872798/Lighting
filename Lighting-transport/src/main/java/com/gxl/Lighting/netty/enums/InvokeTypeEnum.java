package com.gxl.Lighting.netty.enums;

import com.gxl.Lighting.netty.codec.LightingCodec;

public enum InvokeTypeEnum {

    ONEWAY(LightingCodec.getONEWAY(), "O"),
    SYNC(LightingCodec.getSYNC(), "S"),
    ASYNC(LightingCodec.getASYNC(), "A");

    private byte flag;

    private String invokeType;

    InvokeTypeEnum(byte flag, String invokeType){
        this.flag = flag;
        this.invokeType = invokeType;
    }

    public byte getFlag() {
        return flag;
    }

    public String getInvokeType() {
        return invokeType;
    }

    public static byte getValue(String name){
        for(InvokeTypeEnum temp : InvokeTypeEnum.values()){
            if(temp.getInvokeType().equalsIgnoreCase(name)){
                return temp.getFlag();
            }
        }
        return (byte)-1;
    }

    public static String getValue(byte b){
        for(InvokeTypeEnum temp : InvokeTypeEnum.values()){
            if((temp.getFlag() & b ) == temp.getFlag()){
                return temp.getInvokeType();
            }
        }
        return null;
    }

    public static boolean find(String name){
        for(InvokeTypeEnum temp : InvokeTypeEnum.values()){
            if(temp.getInvokeType().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }
}
