package com.gxl.Lighting.proxy;

import com.gxl.Lighting.rpc.RPCInvocation;

import java.lang.reflect.Proxy;

public class ProxyFactory {

    public static Object getProxy(RPCInvocation rpcInvocation, Class[] interfaces){
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, new RPCInvocationHanlder(rpcInvocation));
    }
}
