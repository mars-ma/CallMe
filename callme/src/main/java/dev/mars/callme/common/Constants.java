package dev.mars.callme.common;

import android.os.Environment;

/**
 * Created by ma.xuanwei on 2017/3/28.
 */

public class Constants {
    //默认TCP通信端口
    public static final int TCP_PORT = 5999;
    //默认UDP通信端口
    public static final int UDP_PORT = 5556;

    //默认PCM录制采样率
    public static final int SAMPLERATE = 44100; //bit/s
    //默认PCM录制通道数
    public static final int CHANNELS = 1; //1:单/2:双声道
    //默认PCM录制一帧采样时间
    public static final int PERIOD_TIME = 20; //ms
    //默认PCM录制文件地址
    public static final String DEFAULT_PCM_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/test_pcm.pcm";
    //默认输出PCM文件路径
    public static final String DEFAULT_PCM_OUTPUT_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/output_pcm.pcm";
    //默认输出speex文件路径
    public static final String DEFAULT_SPEEX_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath()+"/test_speex.raw";

    /**
     * READ_IDLE_TIMEOUT 设置会话读空闲时间
     */
    public static final int READ_IDLE_TIMEOUT = 35; //{@link READ_IDLE_TIMEOUT}

    /**
     * WRITE_IDLE_TIMEOUT 设置会话写空闲时间，当会话写空闲发送心跳包给服务器
     */
    public static final int WRITE_IDLE_TIMEOUT = 20;

    /**
     * 发生READ_IDLE_TIMES次 READ IDLE事件后关闭会话
     */
    public static final int READ_IDLE_CLOSE_TIMES = 1;
}
