package com.gxl.Lighting;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;

public class LoggerTest {
    public static void main(String[] args) {
        InternalLogger i = InternalLoggerFactory.getInstance(LoggerTest.class);
        i.info("你好");
    }
}
