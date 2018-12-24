package com.gxl.Lighting.rpc;

import com.gxl.Lighting.netty.param.InvokeCommandParam;

/**
 * 发起远程调用的 核心接口
 */
public interface RPCInvocation {

    Object invoke(InvokeCommandParam param);
}
