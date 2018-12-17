package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.rpc.CommandParam;

public class InvokerCommandParam extends CommandParam {

    private String methodName;

    private Class<?>[] paramTypes;

    private Object[] params;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?>[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(Class<?>[] paramTypes) {
        this.paramTypes = paramTypes;
    }

    public Object[] getParams() {
        return params;
    }

    public void setParams(Object[] params) {
        this.params = params;
    }
}
