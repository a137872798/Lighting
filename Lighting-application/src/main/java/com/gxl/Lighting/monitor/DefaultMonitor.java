package com.gxl.Lighting.monitor;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.monitor.processor.CollectProcessor;
import com.gxl.Lighting.netty.DefaultServer;
import com.gxl.Lighting.netty.RequestEnum;
import com.gxl.Lighting.netty.Server;
import com.gxl.Lighting.netty.param.CollectCommandParam;

import java.util.HashMap;
import java.util.Map;

public class DefaultMonitor implements Monitor{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultMonitor.class);

    private Server server;

    /**
     * 这里的key 是 address,methodName
     */
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

        String key = address + "," + methodName;
        if(statisticInfo.get(key) == null) {
            RPCInfo newRpcInfo = new RPCInfo();
            newRpcInfo.setMethodName(param.getMethodName());
            newRpcInfo.setServiceName(param.getServiceName());
            newRpcInfo.setProvideAddress(param.getAddress());
            statisticInfo.put(key, newRpcInfo);
        }
        RPCInfo rpcInfo = statisticInfo.get(key);
        int oldTimes = rpcInfo.getTimes();
        long oldAverageTime = rpcInfo.getAverageTime();
        long oldSumTime = oldAverageTime * oldTimes;
        int newTimes = oldTimes + 1;
        rpcInfo.setTimes(newTimes);
        long newAverageTime = (oldSumTime + time)/newTimes;
        rpcInfo.setAverageTime(newAverageTime);
        if(isSuccess){
            int successTimes = rpcInfo.getSuccessTimes();
            rpcInfo.setSuccessTimes(++successTimes);
        }else{
            int failTimes = rpcInfo.getFailTimes();
            rpcInfo.setFailTimes(++failTimes);
        }
        int newRate = rpcInfo.getSuccessTimes()/rpcInfo.getTimes();
        rpcInfo.setRate(newRate);
        logger.info("更新" + key + "的监控中心数据,当前数据是" + rpcInfo.toString());
    }

    public Map<String, RPCInfo> getStatisticData() {
        return statisticInfo;
    }

    public RPCInfo getStatisticData(String address) {
        return statisticInfo.get(address);
    }
}
