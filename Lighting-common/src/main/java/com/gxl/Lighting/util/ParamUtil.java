package com.gxl.Lighting.util;

public class ParamUtil {

    public static <T> T checkNotNull(T param, Object msg){
        if(param == null){
            throw  new NullPointerException(String.valueOf(msg));
        }
        return param;
    }
}
