package com.gxl.Lighting.rpc;

import com.gxl.Lighting.consumer.Consumer;
import com.gxl.Lighting.consumer.RPCMeta;
import com.gxl.Lighting.loadbalance.LoadBalance;
import com.gxl.Lighting.loadbalance.RoundRobinLoadBalance;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.netty.Client;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.rpc.param.InvokerCommandParam;
import com.gxl.Lighting.util.StringUtil;

import java.lang.reflect.Method;
import java.util.List;

public class DefaultRPCInvocation implements RPCInvocation{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRPCInvocation.class);

    /**
     * 内部通过 远程通信
     * @return
     */
    private Consumer consumer;

    private LoadBalance balance;

    DefaultRPCInvocation(Consumer consumer){
        this.consumer = consumer;
    }

    /**
     * 进入到这个方法时  RPCMeta 参数的有效性应该是已经检测好的
     * @param param
     * @return
     */
    public Response invoke(InvokerCommandParam param) {
        RPCMeta meta = findServiceNameByMethodInfo(param);
        String serviceName = meta.getServiceName();
        long invokeTimeout = meta.getTimeout();
        String invokeType = meta.getInvokeType();
        if(invokeType.equals(InvokeTypeEnum.ONEWAY.getInvokeType())){
            throw new IllegalArgumentException("rpc调用不允许使用单向通信模式");
        }
        boolean isVip = meta.isVip();
        try {
            balance = meta.getLoadBalance().newInstance();
        } catch (Throwable e) {
            logger.warn("在创建均衡负载对象时出现了未知异常，异常信息为{}使用默认的负载方式",e.getMessage());
            balance = new RoundRobinLoadBalance();
        }
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.INVOKE, param);
        request.setSerialization(meta.getSerialization());
        request.setInvokeType(invokeType);
        List<RegisterMeta> list = consumer.getRegisterInfo(serviceName);
        RegisterMeta providerInfo = balance.select(list);
        String providerAddress = providerInfo.getAddress();

        if(isVip){
            providerAddress = converVip(providerAddress);
        }
        if(invokeType.equals(InvokeTypeEnum.ASYNC.getInvokeType())){
            //TODO  这里需要一个 能生成代理对象的 回调函数
            consumer.getClient().invokeAsync();
        }
        if(invokeType.equals(InvokeTypeEnum.SYNC.getInvokeType())){
            try {
                consumer.getClient().invokeSync(providerAddress, request, invokeTimeout);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemotingSendException e) {
                e.printStackTrace();
            } catch (RemotingTimeoutException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private String converVip(String providerAddress){
        String[] address = StringUtil.split(providerAddress, ":");
        int port = Integer.valueOf(address[1]);
        int vipPort = port + 2;
        address[1] = String.valueOf(vipPort);
        return StringUtil.join(address, ":");
    }

    private RPCMeta findServiceNameByMethodInfo(InvokerCommandParam param){
        Class<?>[] services = consumer.getServices();
        String methodName = param.getMethodName();
        Class<?>[] paramTypes = param.getParamTypes();
        for(Class<?> service : services){

            for(Method method : service.getDeclaredMethods()){

                if(method.getName().equals(methodName) ){
                    Class<?>[] params = method.getParameterTypes();
                    if(params.length == paramTypes.length){

                        for(int i = 0; i < params.length; i++){

                            if(params[i].getName().equals(paramTypes[i].getName())){
                                return consumer.getAnnotationInfo(service);
                            }
                        }
                    }
                }
            }
        }
    }
}
