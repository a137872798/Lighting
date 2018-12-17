package com.gxl.Lighting.netty.serialization;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.enums.SerializationEnum;

/**
 * 获取序列化类的工厂
 */
public class SerializationFactory {

    private SerializationFactory(){}

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(SerializationFactory.class);

    public static Serialization newInstance(String type){
        if(SerializationEnum.JSON.getSerialization().equalsIgnoreCase(type)){
            return JsonSerialization.getINSTANCE();
        }
        if(SerializationEnum.HESSIAN.getSerialization().equalsIgnoreCase(type)){
            return HessianSerialization.getINSTANCE();
        }
        logger.warn("没有找到匹配的序列化对象 默认使用json进行序列化");
        return null;
    }
}
