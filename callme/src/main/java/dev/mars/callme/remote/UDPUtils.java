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

import dev.mars.callme.bean.UDPMessage;
import dev.mars.callme.utils.LogUtils;

/**
 * Created by ma.xuanwei on 2017/3/22.
 */

public class UDPUtils {
    DatagramSocket listenSocket;
    DatagramSocket sendSocket;
    private int port;
    OnReceiveListener onReceiveListener;
    private AtomicBoolean listen = new AtomicBoolean(false);
    WifiManager manager;
    WifiManager.MulticastLock lock_listen;
    ExecutorService service = Executors.newFixedThreadPool(2);
    Handler handler ;

    public void setPort(int p){
        this.port = p;
    }

    public UDPUtils(Context c) {
        handler = new Handler();
        manager = (WifiManager) c
                .getSystemService(Context.WIFI_SERVICE);
        lock_listen = manager.createMulticastLock("listen");

    }

    public void setOnReceiveListener(OnReceiveListener onReceiveListener){
        this.onReceiveListener = onReceiveListener;
    }

    public void send(final String destIP,final UDPMessage msg) {
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
                    String str = msg.toString();
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
                            InetAddress.getByName(destIP), port);
                    LogUtils.D("UDP SEND:" + str+" PORT:"+port);
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

    public void send(final UDPMessage msg) {
        send("255.255.255.255",msg);

    }

    public void stopListening() {
        listen.set(false);
        listenSocket.close();
        listenSocket = null;
        lock_listen.release();
    }

    public void listen() {
        if(listenSocket!=null&&listenSocket.isBound()){
            return;
        }
        try {
            listenSocket = new DatagramSocket(port);
            listenSocket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }

        listen.set(true);
        LogUtils.DT("UDP listen PORT:"+port);
        service.execute(new Runnable() {
            @Override
            public void run() {
                while (listen.get()) {
                    byte[] buf = new byte[100];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);// 创建接收数据报的实例
                    try {
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
                        final String str = new String(dest, "UTF-8");
                        final UDPMessage udpMessage = new UDPMessage();
                        udpMessage.setJSON(str);
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (onReceiveListener != null)
                                    onReceiveListener.onReceive(udpMessage);
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
        void onReceive(UDPMessage msg);
    }
}
