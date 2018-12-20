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
        InvokerCommandParam param = new InvokerCommandParam();

        return null;
    }
}
