package com.gxl.Lighting.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.NOPLoggerFactory;

public class Slf4JLoggerFactory extends InternalLoggerFactory {

    @Override
    protected InternalLogger newInstance(String name) {
        return new Slf4JLogger(name);
    }

    private static class Slf4JLogger implements InternalLogger{
        private Logger logger = null;

        public Slf4JLogger(String name) {
            //在测试中 抛出了一个异常Failed to load class "org.slf4j.impl.StaticLoggerBinder"
//            if (LoggerFactory.getILoggerFactory() instanceof NOPLoggerFactory) {
//                throw new NoClassDefFoundError("NOPLoggerFactory not supported");
//            }
            //使用 slf4j 的 日志工厂生成日志类
            logger = LoggerFactory.getLogger(name);
        }

        public String getName() {
            return logger.getName();
        }

        public void debug(String var1) {
            logger.debug(var1);
        }

        public void debug(String var1, Object var2) {
            logger.debug(var1, var2);
        }

        public void debug(String var1, Object var2, Object var3) {
            logger.debug(var1, var2, var3);
        }

        public void debug(String var1, Object... var2) {
            logger.debug(var1, var2);
        }

        public void debug(String var1, Throwable var2) {
            logger.debug(var1, var2);
        }

        public void info(String var1) {
            logger.info(var1);
        }

        public void info(String var1, Object var2) {
            logger.info(var1, var2);
        }

        public void info(String var1, Object var2, Object var3) {
            logger.info(var1, var2, var3);
        }

        public void info(String var1, Object... var2) {
            logger.info(var1, var2);
        }

        public void info(String var1, Throwable var2) {
            logger.info(var1, var2);
        }

        public void warn(String var1) {
            logger.info(var1);
        }

        public void warn(String var1, Object var2) {
            logger.info(var1, var2);
        }

        public void warn(String var1, Object... var2) {
            logger.info(var1, var2);
        }

        public void warn(String var1, Object var2, Object var3) {
            logger.warn(var1, var2, var3);
        }

        public void warn(String var1, Throwable var2) {
            logger.warn(var1, var2);
        }

        public void error(String var1) {
            logger.error(var1);
        }

        public void error(String var1, Object var2) {
            logger.error(var1, var2);
        }

        public void error(String var1, Object var2, Object var3) {
            logger.error(var1, var2, var3);
        }

        public void error(String var1, Object... var2) {
            logger.error(var1, var2);
        }

        public void error(String var1, Throwable var2) {
            logger.error(var1, var2);
        }
    }
}
