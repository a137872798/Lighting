package com.gxl.Lighting.rpc;

import com.gxl.Lighting.consumer.DefaultConsumer;
import com.gxl.Lighting.loadbalance.LoadBalance;
import com.gxl.Lighting.loadbalance.RoundRobinLoadBalance;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.meta.RegisterMeta;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.rpc.param.InvokerCommandParam;
import com.gxl.Lighting.util.StringUtil;

import java.lang.reflect.Method;
import java.util.List;

public class DefaultRPCInvocation implements RPCInvocation{

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRPCInvocation.class);

    private final Callback invokeCallBack = new Callback() {
        //有可能用户短时间内调用多次 异步请求 触发回调后就可能出现并发问题
        public synchronized void callback(ResponseFuture future) {
            Response response = future.getResponse();
            if(!future.getResponse().isSuccess()){
                logger.warn("exception{}, msg{}", response.getCause(), response.getErrorMsg());
            }
            consumer.setService(response.create());
        }
    };

    /**
     * 内部通过 远程通信
     * @return
     */
    private DefaultConsumer consumer;

    private LoadBalance balance;

    public DefaultRPCInvocation(DefaultConsumer consumer){
        this.consumer = consumer;
    }

    /**
     * 进入到这个方法时  RPCMeta 参数的有效性应该是已经检测好的
     * @param param
     * @return
     */
    public Object invoke(InvokerCommandParam param) {
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
        //这步的数据是由订阅逻辑生成的
        //TODO 根据订阅到的结果是 1 还是 多个 生成 集群invocation or 单个 invocation
        //TODO 这里需要将 调用失败的 服务器记录下来在下次 负载中跳过这个 访问不到的 服务器
        List<RegisterMeta> list = consumer.getRegisterInfo(serviceName);
        RegisterMeta providerInfo = balance.select(list);
        String providerAddress = providerInfo.getAddress();

        if(isVip){
            providerAddress = converVip(providerAddress);
        }
        if(invokeType.equals(InvokeTypeEnum.ASYNC.getInvokeType())){
            consumer.getClient().invokeAsync(providerAddress, request, invokeCallBack, invokeTimeout);
            //异步调用 直接返回null 结果到达之后 会通过回调 设置结果
            return null;
        }

        Response response = null;
        if(invokeType.equals(InvokeTypeEnum.SYNC.getInvokeType())){
            try {
                response = consumer.getClient().invokeSync(providerAddress, request, invokeTimeout);
                if(!response.isSuccess()){
                    logger.warn("exception{}, msg{}", response.getCause(), response.getErrorMsg());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemotingSendException e) {
                e.printStackTrace();
            } catch (RemotingTimeoutException e) {
                e.printStackTrace();
            }
        }

        return response.create();
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
        logger.warn("在目标服务上没有找到对应的RPCMeta信息");
        return null;
    }
}
