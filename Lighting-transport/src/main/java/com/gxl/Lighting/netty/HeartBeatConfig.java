package com.gxl.Lighting.netty;

import com.gxl.Lighting.logging.InternalLogger;
import com.gxl.Lighting.logging.InternalLoggerFactory;

/**
 * 保存 心跳检测的 相关参数
 */
public class HeartBeatConfig {

    private static final InternalLogger logger = InternalLoggerFactory.getInstance(HeartBeatConfig.class);

    private int writerIdleTimeSeconds;

    private static final int DEFAULT_WRITERIDLETIMESECONDS = 9;

    private int readerIdleTimeSeconds;

    private static final int DEFAULT_READERIDLETIMESECONDS = 10;

    private int allIdleTimeSeconds;

    private static final int DEFAULT_ALLIDLETIMESECONDS = 10;

    HeartBeatConfig(){
        this(DEFAULT_WRITERIDLETIMESECONDS, DEFAULT_READERIDLETIMESECONDS, DEFAULT_ALLIDLETIMESECONDS);
    }

    HeartBeatConfig(int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds){
        checkParam(writerIdleTimeSeconds, readerIdleTimeSeconds, allIdleTimeSeconds);
        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
        this.allIdleTimeSeconds = allIdleTimeSeconds;
    }

    /**
     * 校验参数有效性
     * @param writerIdleTimeSeconds
     * @param readerIdleTimeSeconds
     * @param allIdleTimeSeconds
     */
    private void checkParam(int writerIdleTimeSeconds, int readerIdleTimeSeconds, int allIdleTimeSeconds) {
        if(writerIdleTimeSeconds <=0 ){
            logger.debug("writerIdleTimeNanos 不能小于等于0");
            throw new IllegalArgumentException("writerIdleTimeSeconds 不能小于等于0");
        }
        if(readerIdleTimeSeconds <= 0){
            logger.debug("readerIdleTimeNanos 不能小于等于0");
            throw new IllegalArgumentException("readerIdleTimeSeconds 不能小于等于0");
        }
        if(allIdleTimeSeconds <= 0){
            logger.debug("allIdleTimeNanos 不能小于等于0");
            throw new IllegalArgumentException("allIdleTimeSeconds 不能小于等于0");
        }
        if(readerIdleTimeSeconds - writerIdleTimeSeconds <= 0){
            logger.debug("基于当前框架 心跳检测的实现 一般写的时间间隔必须小于读的时间间隔");
            throw new IllegalArgumentException("基于当前框架 心跳检测的实现 一般写的时间间隔必须小于读的时间间隔");
        }
    }

    public int getWriterIdleTimeSeconds() {
        return writerIdleTimeSeconds;
    }

    public void setWriterIdleTimeNanos(int writerIdleTimeSeconds) {
        this.writerIdleTimeSeconds = writerIdleTimeSeconds;
    }

    public int getReaderIdleTimeSeconds() {
        return readerIdleTimeSeconds;
    }

    public void setReaderIdleTimeNanos(int readerIdleTimeSeconds) {
        this.readerIdleTimeSeconds = readerIdleTimeSeconds;
    }

    public static int getDefaultWriteridletimeseconds() {
        return DEFAULT_WRITERIDLETIMESECONDS;
    }

    public static int getDefaultReaderidletimeseconds() {
        return DEFAULT_READERIDLETIMESECONDS;
    }

    public static int getDefaultAllidletimeseconds() {
        return DEFAULT_ALLIDLETIMESECONDS;
    }

    public int getAllIdleTimeSeconds() {
        return allIdleTimeSeconds;
    }

    public void setAllIdleTimeNanos(int allIdleTimeSeconds) {
        this.allIdleTimeSeconds = allIdleTimeSeconds;
    }

    @Override
    public String toString() {
        return "HeartBeatConfig{" +
                "writerIdleTimeSeconds=" + writerIdleTimeSeconds+
                ", readerIdleTimeSeconds=" + readerIdleTimeSeconds +
                ", allIdleTimeSeconds=" + allIdleTimeSeconds +
                '}';
    }
}
