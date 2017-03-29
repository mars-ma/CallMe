package dev.mars.callme.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by ma.xuanwei on 2017/3/23.
 */

public class WifiUtils {
    public static String getWifiIP(Context c) {
        WifiManager wifiManager = (WifiManager) c.getSystemService(WIFI_SERVICE);
        //获取当前连接的wifi的信息
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);

    }

    private static String intToIp(int i) {
        return ((i ) & 0xFF) + "."

                + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + (i>>24 & 0xFF);
    }
}
