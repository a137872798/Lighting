package com.gxl.Lighting.loadbalance;

import com.gxl.Lighting.meta.RegisterMeta;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance{

    private Random random = new Random(System.currentTimeMillis());

    public RegisterMeta select(List<RegisterMeta> list) {
        int index = random.nextInt(list.size());
        return list.get(index);
    }
}
