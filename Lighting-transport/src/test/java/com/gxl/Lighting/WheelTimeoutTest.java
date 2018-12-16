package com.gxl.Lighting;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.concurrent.TimeUnit;

public class WheelTimeoutTest {
    public static void main(String[] args) {
        HashedWheelTimer timer = new HashedWheelTimer();
        timer.newTimeout(new TimerTask() {
            public void run(Timeout timeout) throws Exception {
                System.out.println("你好");
            }
        }, 1, TimeUnit.SECONDS);
        timer.start();
    }
}
