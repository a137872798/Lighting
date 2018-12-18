package com.gxl.Lighting.proxy;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.rpc.param.InvokerCommandParam;

import java.lang.reflect.Method;
import java.util.List;

public class Invoker {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(Invoker.class);

    private List<Object> objs;

    public Invoker(List<Object> objs) {
        this.objs = objs;
    }

    public Object invoke(InvokerCommandParam param) {
        String interfaceName = param.getServiceName();
        for (Object o : objs) {
            //暴露的服务是以对象的 接口为单位
            if (o.getClass().getSuperclass().getSimpleName().equals(interfaceName)) {
                try {
                    Method m = o.getClass().getSuperclass().getMethod(param.getMethodName(), param.getParamTypes());
                    Object result = m.invoke(o, param.getParams());
                    return result;
                }catch (Exception e){
                    logger.info(param.toString() + "没有在服务提供者找到合适的服务");
                }
            }
        }
        return null;
    }
}
