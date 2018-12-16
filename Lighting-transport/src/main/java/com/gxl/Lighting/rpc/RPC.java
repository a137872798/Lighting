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
}
