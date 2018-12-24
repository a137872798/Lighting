package com.gxl.Lighting.loadbalance;

import com.gxl.Lighting.netty.meta.RegisterMeta;

import java.util.List;
import java.util.Random;

public class RandomLoadBalance implements LoadBalance{

    private RandomLoadBalance(){}

    private static RandomLoadBalance INSTANCE = new RandomLoadBalance();

    public static RandomLoadBalance getINSTANCE(){
        return INSTANCE;
    }

    private Random random = new Random(System.currentTimeMillis());

    public RegisterMeta select(List<RegisterMeta> list, String oldAddress) {
        RegisterMeta meta = null;
        if(list.size() != 1) {
            for (; ; ) {
                int index = random.nextInt(list.size());
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
