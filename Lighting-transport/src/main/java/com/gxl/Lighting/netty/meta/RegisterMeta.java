package com.gxl.Lighting.netty.meta;


import com.gxl.Lighting.Version;
import java.lang.reflect.Method;
import java.util.Arrays;
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

    @Override
    public String toString() {
        return "RegisterMeta{" +
                "address='" + address + '\'' +
                ", serviceMeta=" + Arrays.toString(serviceMeta) +
                '}';
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
            //默认是 实现单接口
            serviceMeta.setServiceName(clazz.getInterfaces()[0].getSimpleName());
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
