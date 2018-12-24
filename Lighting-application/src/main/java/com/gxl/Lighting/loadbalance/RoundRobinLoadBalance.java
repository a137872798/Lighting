package com.gxl.Lighting.loadbalance;

import com.gxl.Lighting.netty.meta.RegisterMeta;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance {

    private RoundRobinLoadBalance(){}

    private static RoundRobinLoadBalance INSTANCE = new RoundRobinLoadBalance();

    public static RoundRobinLoadBalance getINSTANCE(){
        return INSTANCE;
    }

    private final AtomicInteger count = new AtomicInteger(0);

    public RegisterMeta select(List<RegisterMeta> list, String oldAddress) {
        if (list.size() != 1) {
            RegisterMeta meta = null;
            for (; ; ) {
                int index = count.getAndIncrement();
                index = index % list.size();
                meta = list.get(index);
                if (!meta.getAddress().equals(oldAddress)) {
                    break;
                }
            }
            return meta;
        }
        return list.get(0);
    }
}
