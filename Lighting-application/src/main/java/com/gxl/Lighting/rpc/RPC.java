package com.gxl.Lighting.rpc;

import com.gxl.Lighting.loadbalance.LoadBalance;
import com.gxl.Lighting.loadbalance.RoundRobinLoadBalance;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Documented
@Retention(RetentionPolicy.RUNTIME)
public @interface RPC {

    long timeout() default 1000;

    /**
     * 使用的 负载策略
     * @return
     */
    Class<? extends LoadBalance> balanceStrategy() default RoundRobinLoadBalance.class;

    /**
     * 是否使用vip端口  vip端口的 端口号默认比普通的 多2
     * @return
     */
    boolean vip() default true;


    /**
     * 通信使用的序列化方式
     * @return
     */
    String serialization() default "hessian";

    /**
     * 通信方式  A:async S:sync O:oneWay
     * @return
     */
    String invokeType() default "S";
}
