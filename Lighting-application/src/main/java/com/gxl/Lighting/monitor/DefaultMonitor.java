package com.gxl.Lighting.monitor;

import com.gxl.Lighting.monitor.processor.CollectProcessor;
import com.gxl.Lighting.netty.DefaultServer;
import com.gxl.Lighting.netty.RequestEnum;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.netty.param.CollectCommandParam;

import java.util.HashMap;
import java.util.Map;

public class DefaultMonitor implements Monitor{

    private Server server;

    private Map<String, RPCInfo> statisticInfo =  new HashMap<String, RPCInfo>();

    private static final int DEFAULT_PORT = 100;

    public DefaultMonitor(){
        server = new DefaultServer(DEFAULT_PORT);
        server.start();
        server.getProcessorManager().registerProcessor(RequestEnum.COLLECT, new CollectProcessor(this));
    }

    public synchronized void collect(CollectCommandParam param) {
        String address = param.getAddress();
        boolean isSuccess = param.isSuccess();
        String methodName = param.getMethodName();
        String serviceName = param.getServiceName();
        long time = param.getTime();

        if(statisticInfo.get(address) == null) {
            statisticInfo.put(address, new RPCInfo());
        }
        RPCInfo rpcInfo = statisticInfo.get(address);
        int oldTimes = rpcInfo.getTimes();
        long oldAverageTime = rpcInfo.getAverageTime();
        long oldSumTime = oldAverageTime * oldTimes;
        int newTimes = oldTimes + 1;
        long newAverageTime = (oldSumTime + time)/newTimes;
        if(isSuccess){
            int successTimes = rpcInfo.getSuccessTimes();
            rpcInfo.setSuccessTimes(successTimes++);
        }else{
            int failTimes = rpcInfo.getFailTimes();
            rpcInfo.setFailTimes(failTimes++);
        }
        int newRate = rpcInfo.getSuccessTimes()/rpcInfo.getTimes();
        rpcInfo.setRate(newRate);
    }

    public Map<String, RPCInfo> getStatisticData() {
        return statisticInfo;
    }

    public RPCInfo getStatisticData(String address) {
        return statisticInfo.get(address);
    }
}
