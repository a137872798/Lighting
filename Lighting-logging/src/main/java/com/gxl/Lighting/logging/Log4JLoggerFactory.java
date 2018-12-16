package com.gxl.Lighting.logging;


import org.apache.log4j.Logger;

public class Log4JLoggerFactory extends InternalLoggerFactory {
    protected InternalLogger newInstance(String name) {
        return new Log4JLogger(name);
    }

    private static class Log4JLogger implements InternalLogger {

        private Logger logger = null;

        public Log4JLogger(String name) {
            logger = Logger.getLogger(name);
        }

        public String getName() {
            return logger.getName();
        }

        public void debug(String var1) {
            logger.debug(var1);
        }

        //因为对这个日志框架 不熟 就 简化他的功能

        public void debug(String var1, Object var2) {
            logger.debug(var1);
        }

        public void debug(String var1, Object var2, Object var3) {
            logger.debug(var1);
        }

        public void debug(String var1, Object... var2) {
            logger.debug(var1);
        }

        public void debug(String var1, Throwable var2) {
            logger.debug(var1, var2);
        }

        public void info(String var1) {
            logger.info(var1);
        }

        public void info(String var1, Object var2) {
            logger.info(var1);
        }

        public void info(String var1, Object var2, Object var3) {
            logger.info(var1);
        }

        public void info(String var1, Object... var2) {
            logger.info(var1);
        }

        public void info(String var1, Throwable var2) {
            logger.info(var1, var2);
        }

        public void warn(String var1) {
            logger.info(var1);
        }

        public void warn(String var1, Object var2) {
            logger.info(var1);
        }

        public void warn(String var1, Object... var2) {
            logger.info(var1);
        }

        public void warn(String var1, Object var2, Object var3) {
            logger.warn(var1);
        }

        public void warn(String var1, Throwable var2) {
            logger.warn(var1, var2);
        }

        public void error(String var1) {
            logger.error(var1);
        }

        public void error(String var1, Object var2) {
            logger.error(var1);
        }

        public void error(String var1, Object var2, Object var3) {
            logger.error(var1);
        }

        public void error(String var1, Object... var2) {
            logger.error(var1);
        }

        public void error(String var1, Throwable var2) {
            logger.error(var1, var2);
        }
    }
}
