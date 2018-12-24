package com.gxl.Lighting;

import com.gxl.Lighting.consumer.Consumer;
import com.gxl.Lighting.consumer.DefaultConsumer;
import com.gxl.Lighting.monitor.DefaultMonitor;
import com.gxl.Lighting.monitor.Monitor;
import com.gxl.Lighting.provider.DefaultProvider;
import com.gxl.Lighting.provider.Provider;


public class Test {

    public static void main(String[] args) throws Exception{
        Consumer consumer = new DefaultConsumer();
        consumer.addSubscribeService(Human.class);
        Registry registry = new DefaultRegistry();
        Provider provider = new DefaultProvider();
        provider.setRegistryAddresses(new String[]{"127.0.0.1:102"});
        consumer.setRegistryAddresses(new String[]{"127.0.0.1:102"});
        consumer.setMonitorAddress("127.0.0.1:100");
        //publishService 的 下标必须 和 ref 的下标对应
        provider.setRef(new Man());
        provider.addPublishService(Man.class);
        Monitor monitor = new DefaultMonitor();
        registry.start();
        provider.publish();
        consumer.subscribe();
        //需要等待 各个组件启动完毕 并发送数据后才能启动
        Thread.sleep(3000);
        Human human = (Human)consumer.getService();
        System.out.println(human.sayHello("你好"));

    }
}
