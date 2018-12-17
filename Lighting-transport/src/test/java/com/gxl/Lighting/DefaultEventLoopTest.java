package com.gxl.Lighting;

import io.netty.channel.DefaultEventLoop;

import javax.swing.plaf.TableHeaderUI;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class DefaultEventLoopTest {
    public static void main(String[] args) {
        DefaultEventLoop loop = new DefaultEventLoop();
        final ScheduledFuture future = loop.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                System.out.println("你好");
            }
        },0,1, TimeUnit.SECONDS);
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(5000);
                    future.cancel(false);
                    System.out.println("关闭定时任务");
                }catch (Exception e){

                }
            }
        });
        t.start();
    }
}
