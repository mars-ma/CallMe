package dev.mars.callme.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

import dev.mars.callme.BuildConfig;

/**
 * Created by ma.xuanwei on 2016/12/6.
 */

/**
 * 用于在DEBUG模式下输出Log
 */
public class LogUtils {
    private static final boolean DEBUG = dev.mars.callme.BuildConfig.DEBUG;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static String TAG = "dev_mars";
    public static void setTAG(String tag){
        TAG = tag;
    }

    public static void E(String msg){
        if (DEBUG){
            Log.e(TAG,msg);
        }
    }

    public static void E(String tag,String msg){
        if (DEBUG){
            Log.e(tag,msg);
        }
    }

    public static void D(String msg){
        if (DEBUG){
            Log.d(TAG,msg);
        }
    }

    public static void DT(String msg){
        if (DEBUG){
            Log.d(TAG,sdf.format(new Date())+":"+msg);
        }
    }

    public static void I(String msg){
        if (DEBUG){
            Log.i(TAG,msg);
        }
    }
}
