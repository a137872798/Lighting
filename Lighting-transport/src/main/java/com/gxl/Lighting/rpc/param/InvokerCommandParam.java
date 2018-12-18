package com.gxl.Lighting.rpc.param;

import com.gxl.Lighting.rpc.CommandParam;

import java.util.Arrays;

public class InvokerCommandParam extends CommandParam {

    /**
     * 需要知道调用的是哪个类的服务实现
     */
    private String serviceName;

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

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "InvokerCommandParam{" +
                "serviceName='" + serviceName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", paramTypes=" + Arrays.toString(paramTypes) +
                ", params=" + Arrays.toString(params) +
                '}';
    }
}
