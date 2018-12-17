package com.gxl.Lighting.rpc;

public class DefaultInvoker {
    public static RPCProxy invoke(RPC request) {
        RPCParam param = request.getRpcParam();
        String methodName = param.getName();
        Class<?>[] types = param.getParamTypes();
        Object[] params = param.getParams();
    }
}
