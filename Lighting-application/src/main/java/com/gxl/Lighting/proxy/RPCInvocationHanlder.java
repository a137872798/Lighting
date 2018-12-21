package com.gxl.Lighting.proxy;

import com.gxl.Lighting.rpc.RPCInvocation;
import com.gxl.Lighting.rpc.param.InvokerCommandParam;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public class RPCInvocationHanlder implements InvocationHandler{

    RPCInvocation rpcInvocation;

    public RPCInvocationHanlder(RPCInvocation rpcInvocation){
        this.rpcInvocation = rpcInvocation;
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //设置 rpc 参数对象
        InvokerCommandParam param = new InvokerCommandParam();
        param.setMethodName(method.getName());
        param.setParamTypes(method.getParameterTypes());
        param.setParams(args);

        //开始远程通信

        return rpcInvocation.invoke(param);
    }
}
