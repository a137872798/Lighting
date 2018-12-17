package com.gxl.Lighting.util;

public class BytesUtil {

    private BytesUtil(){}

    public static void short2byte(short v, byte[] dst, int index){
        dst[index+1] = (byte)v;
        dst[index] = (byte)(v>>>8);
    }

    public static void short2byte(short v, byte[] dst){
        short2byte(v, dst,0);
    }

    public static byte[] short2byte(short v){
        byte[] dst = new byte[]{0,0};
        short2byte(v, dst);
        return dst;
    }

    public static byte[] long2byte(long v, byte[] dst, int index){
        dst[index + 7] = (byte) v;
        dst[index + 6] = (byte) (v >>> 8);
        dst[index + 5] = (byte) (v >>> 16);
        dst[index + 4] = (byte) (v >>> 24);
        dst[index + 3] = (byte) (v >>> 32);
        dst[index + 2] = (byte) (v >>> 40);
        dst[index + 1] = (byte) (v >>> 48);
        dst[index + 0] = (byte) (v >>> 56);

        return dst;
    }

    public static void int2byte(int v, byte[] dst, int index){
        dst[index + 3] = (byte) v;
        dst[index + 2] = (byte) (v >>> 8);
        dst[index + 1] = (byte) (v >>> 16);
        dst[index + 0] = (byte) (v >>> 24);
    }

    public static int bytes2int(byte[] b, int off) {
        return ((b[off + 3] & 0xFF) << 0) +
                ((b[off + 2] & 0xFF) << 8) +
                ((b[off + 1] & 0xFF) << 16) +
                ((b[off + 0]) << 24);
    }

    public static void long2bytes(long v, byte[] b, int off) {
        b[off + 7] = (byte) v;
        b[off + 6] = (byte) (v >>> 8);
        b[off + 5] = (byte) (v >>> 16);
        b[off + 4] = (byte) (v >>> 24);
        b[off + 3] = (byte) (v >>> 32);
        b[off + 2] = (byte) (v >>> 40);
        b[off + 1] = (byte) (v >>> 48);
        b[off + 0] = (byte) (v >>> 56);
    }

    public static long bytes2long(byte[] b, int off) {
        return ((b[off + 7] & 0xFFL) << 0) +
                ((b[off + 6] & 0xFFL) << 8) +
                ((b[off + 5] & 0xFFL) << 16) +
                ((b[off + 4] & 0xFFL) << 24) +
                ((b[off + 3] & 0xFFL) << 32) +
                ((b[off + 2] & 0xFFL) << 40) +
                ((b[off + 1] & 0xFFL) << 48) +
                (((long) b[off + 0]) << 56);
    }
}
