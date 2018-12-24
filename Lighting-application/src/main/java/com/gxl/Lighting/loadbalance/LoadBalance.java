package com.gxl.Lighting.loadbalance;


import com.gxl.Lighting.netty.meta.RegisterMeta;

import java.util.List;

/**
 * 均衡负载接口  这里默认使用2种很简单的 方法主要还是为了跑通流程
 */
public interface LoadBalance {

    /**
     * 从 给定的列表中选择合适的对象
     * @param list
     * @return
     */
    RegisterMeta select(List<RegisterMeta> list, String oldAddress);
}
