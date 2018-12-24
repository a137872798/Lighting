package com.gxl.Lighting.netty.meta;

import java.util.Arrays;

/**
 * 方法级别元数据
 */
public class MethodMeta {

    private String methodName;

    /**
     * 保存参数类型的全限定名  因为class 数组 在解析成json 时出错  暂时没有解决方案
     */
    private String[] paramsType;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    @Override
    public String toString() {
        return "MethodMeta{" +
                "methodName='" + methodName + '\'' +
                ", paramsType=" + Arrays.toString(paramsType) +
                '}';
    }

    public String[] getParamsType() {
        return paramsType;
    }

    public void setParamsType(String[] paramsType) {
        this.paramsType = paramsType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodMeta that = (MethodMeta) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(paramsType, that.paramsType);
    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(paramsType);
        return result;
    }


}
