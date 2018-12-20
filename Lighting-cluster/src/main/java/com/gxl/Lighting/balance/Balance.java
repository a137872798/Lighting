package com.gxl.Lighting.balance;


import com.gxl.Lighting.meta.RegisterMeta;

import java.util.List;

/**
 * 均衡负载接口
 */
public interface Balance {

    /**
     * 从 给定的列表中选择合适的对象
     * @param list
     * @return
     */
    RegisterMeta select(List<RegisterMeta> list);
}
