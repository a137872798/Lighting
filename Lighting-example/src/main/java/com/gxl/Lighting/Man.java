package com.gxl.Lighting;

import com.alibaba.fastjson.JSON;
import com.gxl.Lighting.rpc.RPC;

@RPC
public class Man implements Human{


    @Override
    public String sayHello(String content) {
        return "RPC返回的结果..........." + content;
    }

}
