package com.gxl.Lighting.rpc;

public class RemotingTimeoutException extends RemotingException {

    public RemotingTimeoutException(String message){
        super(message);
    }

    public RemotingTimeoutException(String message, Throwable t){
        super(message, t);
    }
}
