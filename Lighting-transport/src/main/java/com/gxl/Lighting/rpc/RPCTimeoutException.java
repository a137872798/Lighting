package com.gxl.Lighting.rpc;

public class RPCTimeoutException extends Exception {

    RPCTimeoutException(){ }

    RPCTimeoutException(String message){
        super(message);
    }

    RPCTimeoutException(Throwable t){
        super(t);
    }

    RPCTimeoutException(String message, Throwable t){
        super(message, t);
    }
}
