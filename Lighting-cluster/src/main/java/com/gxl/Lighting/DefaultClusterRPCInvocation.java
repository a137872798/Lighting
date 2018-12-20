package com.gxl.Lighting;

import com.gxl.Lighting.loadbalance.LoadBalance;
import com.gxl.Lighting.rpc.RPCInvocation;
import com.gxl.Lighting.rpc.Response;
import com.gxl.Lighting.rpc.param.InvokerCommandParam;

/**
 * 装饰器模式
 */
public class DefaultClusterRPCInvocation implements CluterRPCInvocation {

    private RPCInvocation invocation;

    private LoadBalance balance;

    public DefaultClusterRPCInvocation(RPCInvocation invocation, LoadBalance balance){
        this.invocation = invocation;
        this.balance = balance;
    }

    public Response invoke(InvokerCommandParam param) {
        return null;
    }
}
