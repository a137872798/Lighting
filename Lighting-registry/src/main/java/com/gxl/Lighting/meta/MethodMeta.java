package com.gxl.Lighting.meta;

/**
 * 方法级别元数据
 */
public class MethodMeta {

    private String methodName;

    private Class<?> params;

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Class<?> getParams() {
        return params;
    }

    public void setParams(Class<?> params) {
        this.params = params;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodMeta that = (MethodMeta) o;

        if (methodName != null ? !methodName.equals(that.methodName) : that.methodName != null) return false;
        return params != null ? params.equals(that.params) : that.params == null;
    }

    @Override
    public int hashCode() {
        int result = methodName != null ? methodName.hashCode() : 0;
        result = 31 * result + (params != null ? params.hashCode() : 0);
        return result;
    }
}
