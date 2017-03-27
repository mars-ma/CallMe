package dev.mars.callme.remote;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.mars.callme.utils.LogUtils;

/**
 * Created by ma.xuanwei on 2017/3/22.
 */

public class UDPUtils {
    DatagramSocket listenSocket;
    DatagramSocket sendSocket;
    int port;
    OnReceiveListener onReceiveListener;
    private AtomicBoolean listen = new AtomicBoolean(false);
    WifiManager manager;
    WifiManager.MulticastLock lock_listen;
    ExecutorService service = Executors.newFixedThreadPool(2);
    Handler handler ;

    public UDPUtils(Context c, int port, OnReceiveListener onReceiveListener) {
        handler = new Handler();
        manager = (WifiManager) c
                .getSystemService(Context.WIFI_SERVICE);
        lock_listen = manager.createMulticastLock("listen");
        this.port = port;
        this.onReceiveListener = onReceiveListener;
        try {
            listenSocket = new DatagramSocket(port);
            listenSocket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void send(final String destIP,final String command) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                if (sendSocket == null) {
                    try {
                        sendSocket = new DatagramSocket();
                        sendSocket.setBroadcast(true);
                    } catch (SocketException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                DatagramPacket dp;
                try {
                    byte[] strBytes = command.getBytes("UTF-8");
                    int length = strBytes.length;
                    byte[] buf = new byte[length + 4];
                    buf[0] = (byte) (length >> 24);
                    buf[1] = (byte) (length >> 16);
                    buf[2] = (byte) (length >> 8);
                    buf[3] = (byte) (length);
                    for (int i = 4; i < length + 4; i++) {
                        buf[i] = strBytes[i - 4];
                    }
                    dp = new DatagramPacket(buf, buf.length,
                            InetAddress.getByName(destIP), port);
                    LogUtils.DT("发送长度:" + length);
                    LogUtils.D("发送:" + command);
                    sendSocket.send(dp);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }

    public void send(final String str) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                if (sendSocket == null) {
                    try {
                        sendSocket = new DatagramSocket();
                        sendSocket.setBroadcast(true);
                    } catch (SocketException e) {
                        e.printStackTrace();
                        return;
                    }
                }
                DatagramPacket dp;
                try {
                    byte[] strBytes = str.getBytes("UTF-8");
                    int length = strBytes.length;
                    byte[] buf = new byte[length + 4];
                    buf[0] = (byte) (length >> 24);
                    buf[1] = (byte) (length >> 16);
                    buf[2] = (byte) (length >> 8);
                    buf[3] = (byte) (length);
                    for (int i = 4; i < length + 4; i++) {
                        buf[i] = strBytes[i - 4];
                    }
                    dp = new DatagramPacket(buf, buf.length,
                            InetAddress.getByName("255.255.255.255"), port);
                    LogUtils.DT("发送长度:" + length);
                    LogUtils.D("发送IP:" + str);
                    sendSocket.send(dp);
                } catch (UnknownHostException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });

    }

    public void stopListen() {
        listen.set(false);
        listenSocket.close();
        lock_listen.release();
    }

    public void listen() {
        listen.set(true);
        service.execute(new Runnable() {
            @Override
            public void run() {
                while (listen.get()) {
                    byte[] buf = new byte[100];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);// 创建接收数据报的实例
                    try {
                        LogUtils.DT("监听消息");
                        lock_listen.acquire();
                        listenSocket.receive(dp);// 阻塞,直到收到数据报后将数据装入IP中
                        int b0 = buf[0];
                        b0 = b0 << 24;
                        int b1 = buf[1];
                        b1 = b1 << 16;
                        int b2 = buf[2];
                        b2 = b2 << 8;
                        int b3 = buf[3];
                        int length = b0 + b1 + b2 + b3;
                        byte[] dest = new byte[length];
                        for (int i = 0; i < length; i++) {
                            dest[i] = buf[i + 4];
                        }
                        LogUtils.DT("收到长度:" + length);
                        final String str = new String(dest, "UTF-8");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onReceiveListener != null)
                                    onReceiveListener.onReceive(str);
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    public interface OnReceiveListener {
        void onReceive(String str);
    }
}
