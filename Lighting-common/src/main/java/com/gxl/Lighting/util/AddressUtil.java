package com.gxl.Lighting.util;

import java.net.InetSocketAddress;

public class AddressUtil {

    private AddressUtil(){}

    public static String socketAddressToAddress(InetSocketAddress address){
        return address.getAddress().getHostAddress() + ":" + address.getPort();
    }
}
