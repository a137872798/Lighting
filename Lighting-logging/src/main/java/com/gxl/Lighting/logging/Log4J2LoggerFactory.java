package com.gxl.Lighting.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Log4J2LoggerFactory extends InternalLoggerFactory {
    protected InternalLogger newInstance(String name) {
        return new Log4J2Logger(name);
    }

    private static class Log4J2Logger implements InternalLogger {

        private Logger logger = null;

        public Log4J2Logger(String name) {
            //使用 Log4j2 的日志管理器生成日志对象
            logger = LogManager.getLogger(name);
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
