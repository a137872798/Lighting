package com.gxl.Lighting.rpc;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RPC {

    long timeout() default 1000;

    /**
     * 使用的 负载策略
     * @return
     */
    String balanceStrategy() default "RoundRobin";

    /**
     * 是否使用特殊端口
     * @return
     */
    boolean vip() default false;

    /**
     * 服务提供者的发布名
     * @return
     */
    String serviceName() default "";

    /**
     * 通信使用的序列化方式
     * @return
     */
    String serialization() default "json";

    /**
     * 通信方式  A:async S:sync O:oneWay
     * @return
     */
    String type() default "S";
}
