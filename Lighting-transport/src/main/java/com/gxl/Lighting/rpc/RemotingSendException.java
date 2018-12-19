package com.gxl.Lighting.rpc;

public class RemotingSendException extends RemotingException{

    public RemotingSendException(String address){
        this("发送数据到" + address + "时失败", null);
    }

    public RemotingSendException(String address, Throwable cause){
        super(address, cause);
    }
}
