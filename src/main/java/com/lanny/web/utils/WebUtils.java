package com.lanny.web.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class WebUtils {

    public static String getLocalIp() {
        try {
            InetAddress ip4 = InetAddress.getLocalHost();
            return ip4.getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return "";
    }
}
