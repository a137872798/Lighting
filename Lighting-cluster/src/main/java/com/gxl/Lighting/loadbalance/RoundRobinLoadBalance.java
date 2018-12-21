package com.gxl.Lighting.loadbalance;

import com.gxl.Lighting.meta.RegisterMeta;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class RoundRobinLoadBalance implements LoadBalance{

    private final AtomicInteger count = new AtomicInteger(0);

    public RegisterMeta select(List<RegisterMeta> list) {
        int index = count.getAndIncrement();
        index = index % list.size();
        return list.get(index);
    }
}
