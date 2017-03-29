package dev.mars.callme.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by ma.xuanwei on 2017/3/22.
 */

public class IPUtils {


    public static String getIP() {
        Enumeration allNetInterfaces;
        InetAddress ip = null;
        try {
            allNetInterfaces = NetworkInterface.getNetworkInterfaces();
            while (allNetInterfaces.hasMoreElements()) {
                NetworkInterface netInterface = (NetworkInterface) allNetInterfaces
                        .nextElement();
                System.out.println(netInterface.getName());
                Enumeration addresses = netInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    ip = (InetAddress) addresses.nextElement();
                    if (ip != null && ip instanceof Inet4Address) {
                        System.out.println("本机的IP = " + ip.getHostAddress());
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        return ip==null?"":ip.getHostAddress();
    }
}
