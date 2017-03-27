package dev.mars.callme.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.utils.LogUtils;

/**
 * Created by ma.xuanwei on 2017/3/27.
 */

public class ClientSocketUtils {
    ExecutorService service = Executors.newCachedThreadPool();
    Socket socket;
    OutputStream os;
    InputStream is;
    ObjectInputStream objIs;
    ObjectOutputStream objOs;
    final int TIME_OUT = 15 * 1000;


    int socket_port;
    IOHandler connectionListener;

    public ClientSocketUtils(int port, IOHandler c) {
        socket_port = port;
        connectionListener = c;
        socket = new Socket();
    }

    public void connect(final String ip) {

        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    SocketAddress socketAddress = new InetSocketAddress(ip, socket_port);
                    LogUtils.DT("尝试连接 "+ip+" : "+socket_port);
                    socket.connect(socketAddress, TIME_OUT);
                    os = socket.getOutputStream();
                    is = socket.getInputStream();
                    objOs = new ObjectOutputStream(os);
                    objIs = new ObjectInputStream(is);
                    startRead();
                    if (connectionListener != null) {
                        connectionListener.onConnected();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    if (connectionListener != null) {
                        connectionListener.onConnectFailed();
                    }
                }
            }
        });
    }

    private void startRead() {
        service.execute(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        SocketMessage sMsg = (SocketMessage) objIs.readObject();
                        if(sMsg==null){
                            LogUtils.DT("读线程退出1");
                            break;
                        }else{
                            handleSocketMessage(sMsg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.DT("读线程退出2");
                        break;
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        LogUtils.DT("读线程退出3");
                        break;
                    }
                }
            }
        });
    }

    private void handleSocketMessage(SocketMessage sMsg) {
        LogUtils.DT("handleSocketMessage command "+sMsg.command);
        /*if(sMsg.datas!=null){
            try {
                LogUtils.DT("datas "+new String(sMsg.datas,"UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }*/
        if(connectionListener!=null)
            connectionListener.onReceiveMessage(sMsg);
    }

    public void send(final SocketMessage msg) {
        service.execute(new Runnable() {
            @Override
            public void run() {
                if (objOs != null) {
                    try {
                        objOs.writeObject(msg);
                        LogUtils.DT("发送消息成功 command " + msg.command);
                    } catch (IOException e) {
                        e.printStackTrace();
                        LogUtils.DT("对象输出流异常 " + e.getMessage());
                        close();
                        if(connectionListener!=null){
                            connectionListener.onConnectionClosed();
                        }
                    }
                } else {
                    LogUtils.DT("对象输出流不存在");
                }
            }
        });
    }

    public void close() {
        try {
            if (objOs != null) {
                objOs.flush();
                objOs.close();
            }
            if(objIs!=null){
                objIs.close();
            }
            if(os!=null){
                os.flush();
                os.close();
            }
            if(is!=null){
                is.close();
            }
            if(socket!=null){
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
