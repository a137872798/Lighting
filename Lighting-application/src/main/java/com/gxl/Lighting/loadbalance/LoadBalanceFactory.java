package com.gxl.Lighting.loadbalance;

public class LoadBalanceFactory {

    public static LoadBalance newInstance(Class<?> clazz){
        if(clazz.isAssignableFrom(RoundRobinLoadBalance.class)){
            return RoundRobinLoadBalance.getINSTANCE();
        }else if(clazz.isAssignableFrom(RandomLoadBalance.class)){
            return RandomLoadBalance.getINSTANCE();
        }
        return null;
    }
}
