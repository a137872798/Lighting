package com.gxl.Lighting.proxy;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.param.InvokeCommandParam;


import java.lang.reflect.Method;
import java.util.List;

public class Invoker {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Invoker.class);



    private List<Class<?>> exports;

    private List<Object> refs;

    public Invoker(List<Class<?>> exports, List<Object> refs) {
        this.exports = exports;
        this.refs = refs;
    }

    public Object invoke(InvokeCommandParam param) throws Exception {
        String interfaceName = param.getServiceName();
//        for (Class<?> o : exports) {
        for(int i = 0 ; i < exports.size() ; i ++){
            //暴露的服务是以对象的 接口为单位
//            if (o.getSuperclass().getSimpleName().equals(interfaceName)) {
            for (Class clazz : exports.get(i).getInterfaces()) {
                if (clazz.getSimpleName().equals(interfaceName)) {
                    try {
                        Class[] parameterType = null;
                        for (int j = 0; j < param.getParamTypes().length; j++) {
                            parameterType = new Class[param.getParamTypes().length];
                            parameterType[j] = Class.forName(param.getParamTypes()[j]);
                        }
                        Method m = exports.get(i).getDeclaredMethod(param.getMethodName(), parameterType);
                        Object result = m.invoke(refs.get(i), param.getParams());
                        return result;
                    } catch (Exception e) {
                        logger.info(param.toString() + "没有在服务提供者找到合适的服务");
                        throw e;
                    }
                }
            }
        }
        return null;
    }
}
