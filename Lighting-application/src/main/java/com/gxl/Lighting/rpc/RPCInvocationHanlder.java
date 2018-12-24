package com.gxl.Lighting.rpc;


import com.gxl.Lighting.netty.param.InvokeCommandParam;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RPCInvocationHanlder implements InvocationHandler{

    RPCInvocation rpcInvocation;

    public RPCInvocationHanlder(RPCInvocation rpcInvocation){
        this.rpcInvocation = rpcInvocation;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //设置 rpc 参数对象
        InvokeCommandParam param = new InvokeCommandParam();
        param.setMethodName(method.getName());
        String[] parameterTypes = null;
        for(int i = 0 ;i < method.getParameterTypes().length ; i++){
            parameterTypes = new String[method.getParameterTypes().length];
            parameterTypes[i] = method.getParameterTypes()[i].getName();
        }
        param.setParamTypes(parameterTypes);

        param.setParams(args);

        //开始远程通信

        return rpcInvocation.invoke(param);
    }
}
