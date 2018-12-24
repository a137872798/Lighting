package com.gxl.Lighting.util;

public class StringUtil {
    private StringUtil(){}

    public static boolean isEmpty(String str){
        if(str == null || str.equals("")){
            return true;
        }
        return false;
    }

    public static String[] split(String str, String regex){
        String[] result = str.split(regex);
        return result;
    }

    public static String join(String[] strs, String split){
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < strs.length; i++){
            if(i == 0){
                sb.append(strs[0]);
            } else {
                sb.append(split + strs[i]);
            }
        }
        return sb.toString();
    }
}
