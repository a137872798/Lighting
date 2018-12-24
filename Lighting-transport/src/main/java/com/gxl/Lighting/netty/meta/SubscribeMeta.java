package com.gxl.Lighting.netty.meta;


import com.gxl.Lighting.Version;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * 订阅者元数据
 */
public class SubscribeMeta {

    private ServiceMeta[] serviceMeta;

    /**
     * 在注册中心通过这个地址来识别客户端
     */
    private String address;

    @Override
    public String toString() {
        return "SubscribeMeta{" +
                "serviceMeta=" + Arrays.toString(serviceMeta) +
                ", address='" + address + '\'' +
                '}';
    }

    public ServiceMeta[] getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta[] serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public static SubscribeMeta newMeta(List<Class<?>> list) {
        SubscribeMeta meta = new SubscribeMeta();
        ServiceMeta[] serviceMetas = new ServiceMeta[list.size()];
        for(int i = 0 ; i < list.size() ; i++){
            Class<?> clazz = list.get(i);
            ServiceMeta serviceMeta = new ServiceMeta();
            serviceMeta.setServiceName(clazz.getSimpleName());
            serviceMeta.setVersion(Version.version());
            Method[] methods = clazz.getDeclaredMethods();
            MethodMeta[] methodMetas = new MethodMeta[methods.length];
            for(int j = 0 ; j < methods.length ; j++){
                Method method = methods[j];
                MethodMeta methodMeta = new MethodMeta();
                methodMeta.setMethodName(method.getName());
                Class[] paramTypes = method.getParameterTypes();
                String[] types = new String[paramTypes.length];
                for(int k = 0 ; k < paramTypes.length ; k++){
                    types[k] = paramTypes[k].getName();
                }
                methodMeta.setParamsType(types);
                methodMetas[j] = methodMeta;
            }
            serviceMeta.setMethodMeta(methodMetas);
            serviceMetas[i] = serviceMeta;
        }
        meta.setServiceMeta(serviceMetas);
        return meta;
    }
}
