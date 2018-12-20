package com.gxl.Lighting.rpc;

import com.gxl.Lighting.rpc.param.InvokerCommandParam;

/**
 * 发起远程调用的 核心接口
 */
public interface RPCInvocation {

    Response invoke(InvokerCommandParam param);
}
