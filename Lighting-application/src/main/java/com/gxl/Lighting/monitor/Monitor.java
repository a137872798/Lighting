package com.gxl.Lighting.monitor;

import com.gxl.Lighting.netty.param.CollectCommandParam;

import java.util.Map;

/**
 * 监控中心接口
 */
public interface Monitor {

    void collect(CollectCommandParam param);

    /**
     * 根据服务名 查找调用统计信息
     * @return
     */
    Map<String, RPCInfo> getStatisticData();

    RPCInfo getStatisticData(String address);
}
