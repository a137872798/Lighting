package com.gxl.Lighting.meta;


import com.gxl.Lighting.Version;
import java.lang.reflect.Method;
import java.util.List;

/**
 * 服务注册后生成的 元数据
 */
public class RegisterMeta {

    private String address;

    private ServiceMeta[] serviceMeta;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ServiceMeta[] getServiceMeta() {
        return serviceMeta;
    }

    public void setServiceMeta(ServiceMeta[] serviceMeta) {
        this.serviceMeta = serviceMeta;
    }

    public static RegisterMeta newMeta(List<Class<?>> list, String address) {
        RegisterMeta meta = new RegisterMeta();
        meta.setAddress(address);
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
                methodMeta.setParamsType(method.getParameterTypes());
                methodMetas[j] = methodMeta;
            }
            serviceMeta.setMethodMeta(methodMetas);
            serviceMetas[i] = serviceMeta;
        }

        meta.setServiceMeta(serviceMetas);
        return meta;
    }

}
