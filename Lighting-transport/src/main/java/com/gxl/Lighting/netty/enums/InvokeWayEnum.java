package com.gxl.Lighting.netty.enums;

import com.gxl.Lighting.netty.codec.LightingCodec;

public enum InvokeWayEnum {

    ONEWAY(LightingCodec.getONEWAY(), "O"),
    SYNC(LightingCodec.getSYNC(), "S"),
    ASYNC(LightingCodec.getASYNC(), "A");

    private byte flag;

    private String invokeWay;

    InvokeWayEnum(byte flag, String invokeWay){
        this.flag = flag;
        this.invokeWay = invokeWay;
    }

    public byte getFlag() {
        return flag;
    }

    public String getInvokeWay() {
        return invokeWay;
    }

    public static byte getValue(String invokeWay){
        for(InvokeWayEnum temp : InvokeWayEnum.values()){
            if(temp.getInvokeWay().equalsIgnoreCase(invokeWay)){
                return temp.getFlag();
            }
        }
        return (byte)-1;
    }

    public static String getValue(byte b){
        for(InvokeWayEnum temp : InvokeWayEnum.values()){
            if(temp.getFlag() == b){
                return temp.getInvokeWay();
            }
        }
        return null;
    }
}
