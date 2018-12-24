package com.gxl.Lighting.netty.param;

import com.gxl.Lighting.netty.CommandParam;

import java.util.Arrays;

public class InvokeCommandParam implements CommandParam {

    /**
     * 需要知道调用的是哪个类的服务实现
     */
    private String serviceName;

    private String methodName;

    /**
     * json 不能反序列化 class[]对象 所以 替换成String 数组 暂时没有解决方案
     */
    private String[] paramTypes;

    private Object[] params;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String[] getParamTypes() {
        return paramTypes;
    }

    public void setParamTypes(String[] paramTypes) {
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
