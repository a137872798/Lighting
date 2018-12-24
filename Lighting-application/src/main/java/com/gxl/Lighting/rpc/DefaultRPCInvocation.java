package com.gxl.Lighting.rpc;

import com.gxl.Lighting.NamedThreadFactory;
import com.gxl.Lighting.netty.*;
import com.gxl.Lighting.consumer.DefaultConsumer;
import com.gxl.Lighting.loadbalance.LoadBalance;
import com.gxl.Lighting.loadbalance.LoadBalanceFactory;
import com.gxl.Lighting.loadbalance.RoundRobinLoadBalance;
import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;
import com.gxl.Lighting.netty.meta.RegisterMeta;
import com.gxl.Lighting.monitor.StatisticContext;
import com.gxl.Lighting.netty.enums.InvokeTypeEnum;
import com.gxl.Lighting.netty.param.CollectCommandParam;
import com.gxl.Lighting.netty.param.InvokeCommandParam;
import com.gxl.Lighting.util.StringUtil;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.Timer;
import io.netty.util.TimerTask;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.*;

public class DefaultRPCInvocation implements RPCInvocation {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(DefaultRPCInvocation.class);

    private Timer timer = new HashedWheelTimer(new NamedThreadFactory("rpcInvoke.thread", true));

    /**
     * 处理异步统计信息
     */
    private ConcurrentMap<Long, StatisticContext> statistic = new ConcurrentHashMap<Long, StatisticContext>();

    private TimerTask task = new TimerTask() {
        public void run(Timeout timeout) throws Exception {
            RPCRetry retry = queue.take();
            if (!consumer.isShutdown()) {
                consumer.getClient().invokeAsync(retry.getAddress(), retry.getRequest(), invokeCallBack, retry.getTimeout());
                timer.newTimeout(this, 100, TimeUnit.MILLISECONDS);
            }
        }
    };

    private BlockingQueue<RPCRetry> queue = new LinkedBlockingQueue();

    private final Callback invokeCallBack = new Callback() {
        //有可能用户短时间内调用多次 异步请求 触发回调后就可能出现并发问题
        public synchronized void callback(ResponseFuture future) throws RemotingException {
            Response response = future.getResponse();
            long end = System.currentTimeMillis();
            StatisticContext ctx = statistic.get(response.getId());
            long useTime = end = ctx.getStartTime();
            String methodName = ctx.getMethodName();
            String serviceName = ctx.getServiceName();
            String address = ctx.getAddress();
            long timout = ctx.getTimeout();
            Request request = ctx.getRequest();
            List<RegisterMeta> list = ctx.getMetas();
            CollectCommandParam param = new CollectCommandParam();
            param.setTime(useTime);
            param.setServiceName(serviceName);
            param.setMethodName(methodName);
            param.setAddress(address);
            if (!future.getResponse().getResult().isSuccess()) {
                logger.warn("异步调用{}，出错exception{}, msg{}", address, response.getResult().getCause(), response.getResult().getErrorMsg());
                RPCRetry retry = new RPCRetry();
                retry.setTimeout(timout);
                retry.setRequest(request);
                String newAddress = balance.select(list, address).getAddress();
                retry.setAddress(newAddress);
                param.setSuccess(false);
                logger.info("将数据发往监控中心"+ param.toString());
                consumer.getClient().oneWay(consumer.getMonitorAddress(), Request.createRequest(RequestEnum.COLLECT, param));
                queue.add(retry);
            } else {
                param.setSuccess(true);
                logger.info("将数据发往监控中心"+ param.toString());
                consumer.getClient().oneWay(consumer.getMonitorAddress(), Request.createRequest(RequestEnum.COLLECT, param));
                consumer.setService(response.getResult().getResult());
            }
        }
    };

    /**
     * 内部通过 远程通信
     *
     * @return
     */
    private DefaultConsumer consumer;

    private LoadBalance balance;

    public DefaultRPCInvocation(DefaultConsumer consumer) {
        this.consumer = consumer;
        timer.newTimeout(task, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * 进入到这个方法时  RPCMeta 参数的有效性应该是已经检测好的
     *
     * @param param
     * @return
     */
    public Object invoke(InvokeCommandParam param) {
        RPCMeta meta = findServiceNameByMethodInfo(param);
        String serviceName = meta.getServiceName();
        long invokeTimeout = meta.getTimeout();
        String invokeType = meta.getInvokeType();
        if (invokeType.equals(InvokeTypeEnum.ONEWAY.getInvokeType())) {
            throw new IllegalArgumentException("rpc调用不允许使用单向通信模式");
        }
        boolean isVip = meta.isVip();
        balance = LoadBalanceFactory.newInstance(meta.getLoadBalance());
        if (balance == null) {
            logger.info("在创建均衡负载对象时出现了未知异常，异常信息为{}使用默认的负载方式");
            balance = LoadBalanceFactory.newInstance(RoundRobinLoadBalance.class);
        }
        param.setServiceName(serviceName);
        Request request = Request.createRequest(RequestEnum.INVOKE, param);
        request.setSerialization(meta.getSerialization());
        request.setInvokeType(invokeType);
        List<RegisterMeta> list = consumer.getRegisterInfo(serviceName);
        RegisterMeta providerInfo = balance.select(list, null);
        String providerAddress = providerInfo.getAddress();

        if (isVip) {
            providerAddress = converVip(providerAddress);
        }

        if (invokeType.equals(InvokeTypeEnum.ASYNC.getInvokeType())) {
            StatisticContext ctx = new StatisticContext();
            long start = System.currentTimeMillis();
            ctx.setMethodName(param.getMethodName());
            ctx.setServiceName(param.getServiceName());
            ctx.setStartTime(start);
            ctx.setAddress(providerAddress);
            ctx.setTimeout(invokeTimeout);
            ctx.setRequest(request);
            ctx.setMetas(list);
            statistic.putIfAbsent(request.getId(), ctx);
            consumer.getClient().invokeAsync(providerAddress, request, invokeCallBack, invokeTimeout);
            //异步调用 直接返回null 结果到达之后 会通过回调 设置结果
            return null;
        }

        Response response = null;
        if (invokeType.equals(InvokeTypeEnum.SYNC.getInvokeType())) {
            try {
                CollectCommandParam collect = new CollectCommandParam();
                collect.setAddress(providerAddress);
                collect.setMethodName(param.getMethodName());
                collect.setServiceName(param.getServiceName());
                collect.setAddress(providerAddress);
                long start = System.currentTimeMillis();
                response = consumer.getClient().invokeSync(providerAddress, request, invokeTimeout);
                long end = System.currentTimeMillis();
                long useTime = end - start;
                collect.setTime(useTime);
                if (!response.getResult().isSuccess()) {
                    logger.warn("访问地址{}时出现：exception{}, msg{}", providerAddress, response.getResult().getCause(), response.getResult().getErrorMsg());
                    logger.info("更换地址后进行重试");
                    String newAddress = balance.select(list, providerAddress).getAddress();
                    RPCRetry retry = new RPCRetry();
                    retry.setAddress(newAddress);
                    request.setInvokeType(InvokeTypeEnum.ASYNC.getInvokeType());
                    retry.setRequest(request);
                    retry.setTimeout(invokeTimeout);
                    queue.add(retry);
                    collect.setSuccess(false);
                    logger.info("将数据发往监控中心"+ collect.toString());
                    consumer.getClient().oneWay(consumer.getMonitorAddress(), Request.createRequest(RequestEnum.COLLECT, collect));
                    return null;
                } else {
                    collect.setSuccess(true);
                    logger.info("将数据发往监控中心"+ collect.toString());
                    consumer.getClient().oneWay(consumer.getMonitorAddress(), Request.createRequest(RequestEnum.COLLECT, collect));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (RemotingSendException e) {
                e.printStackTrace();
            } catch (RemotingTimeoutException e) {
                e.printStackTrace();
            } catch (RemotingException e) {
                e.printStackTrace();
            }
        }

        return response.getResult().getResult();
    }

    private String converVip(String providerAddress) {
        String[] address = StringUtil.split(providerAddress, ":");
        int port = Integer.valueOf(address[1]);
        int vipPort = port + 2;
        address[1] = String.valueOf(vipPort);
        return StringUtil.join(address, ":");
    }

    private RPCMeta findServiceNameByMethodInfo(InvokeCommandParam param) {
        Class<?>[] services = consumer.getServices();
        String methodName = param.getMethodName();
        String[] paramTypes = param.getParamTypes();
        for (Class<?> service : services) {

            for (Method method : service.getDeclaredMethods()) {

                if (method.getName().equals(methodName)) {
                    Class<?>[] params = method.getParameterTypes();
                    if (params.length == paramTypes.length) {

                        for (int i = 0; i < params.length; i++) {

                            if (params[i].getName().equals(paramTypes[i])) {
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
