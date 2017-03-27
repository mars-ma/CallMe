package dev.mars.callme.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.remote.ClientSocketUtils;
import dev.mars.callme.remote.IOHandler;
import dev.mars.callme.remote.ServerSocketUtils;
import dev.mars.callme.remote.UDPUtils;
import dev.mars.callme.utils.LogUtils;
import dev.mars.callme.utils.WifiUtils;

public class SocketService extends Service {

    UDPUtils udpUtils;
    public static final int UDP_PORT=5556;
    public static final int TCP_PORT=5557;
    public static final int COMMAND_SEND_LOCAL_IP=0;
    public static final int COMMAND_END_LOCAL=1;
    public static final int COMMAND_SEND_TEXT=2;
    public static int STATE = 0; //0:默认 1:等待通信 2:建立通信
    private int MODE = -1; //-1默认,0表示客户端，1表示服务端

    private ServerSocketUtils serverSocketUtils;
    private ClientSocketUtils clientSocketUtils;
    public SocketService() {
    }

    public static void start(Context context){
        Intent intent = new Intent(context,SocketService.class);
        context.startService(intent);
    }

    public static void sendLocalIP(Context context){
        Intent intent = new Intent(context,SocketService.class);
        intent.putExtra("command",COMMAND_SEND_LOCAL_IP);
        context.startService(intent);
    }

    private void startSocketListen(){
        if(STATE==0) {
            serverSocketUtils = new ServerSocketUtils(TCP_PORT, new IOHandler() {
                @Override
                public void onConnected() {
                    STATE = 2;
                    MODE = 1;
                    LogUtils.DT("服务端建立连接成功");

                    SocketMessage msg = new SocketMessage();
                    msg.command=0;
                    try {
                        msg.datas = "from server".getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    serverSocketUtils.send(msg);
                }

                @Override
                public void onConnectFailed() {
                    STATE = 0;
                    MODE = -1;
                    LogUtils.DT("服务端建立连接失败");
                }

                @Override
                public void onConnectionClosed() {
                    STATE = 0;
                    MODE = -1;
                    LogUtils.DT("服务端连接关闭");
                }

                @Override
                public void onReceiveMessage(SocketMessage msg) {
                    if(msg.command==0){
                        //文本消息
                        try {
                            EventBus.getDefault().post(new String(msg.datas,"UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            serverSocketUtils.listen();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        udpUtils = new UDPUtils(getBaseContext(), UDP_PORT, new UDPUtils.OnReceiveListener() {
            @Override
            public void onReceive(String str) {
                LogUtils.DT("onReceive " + str);
                String cmd_from,ip_from =null;
                try {
                    JSONObject from =  new JSONObject(str);
                    cmd_from = from.getString("cmd");
                    ip_from = from.getString("ip");
                } catch (JSONException e) {
                    e.printStackTrace();
                    return;
                }

                if("start_socket_connection".equals(cmd_from)&&ip_from!=null&&!ip_from.equals(WifiUtils.getWifiIP(getBaseContext()))){

                    LogUtils.DT("客户端向 "+ip_from+" 发起请求。");
                    startSocketConnect(ip_from);
                    STATE=1;
                }else if("broadcast_ip".equals(cmd_from)&&ip_from!=null&&!ip_from.equals(WifiUtils.getWifiIP(getBaseContext()))){

                    startSocketListen();
                    STATE=1;
                    LogUtils.DT("服务端开始监听");
                    JSONObject json = new JSONObject();
                    try {
                        json.put("cmd","start_socket_connection");
                        json.put("ip",WifiUtils.getWifiIP(getBaseContext()));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    udpUtils.send(ip_from,json.toString());
                }
            }
        });
        udpUtils.listen();
    }

    private void startSocketConnect(final String ip){
        if(STATE==0) {
            clientSocketUtils = new ClientSocketUtils(TCP_PORT, new IOHandler() {
                @Override
                public void onConnected() {
                    STATE = 2;
                    MODE = 0;
                    LogUtils.DT("客户端与 "+ip+" 建立连接");
                    SocketMessage msg = new SocketMessage();
                    msg.command=0;
                    try {
                        msg.datas = "from client".getBytes("UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    clientSocketUtils.send(msg);
                }

                @Override
                public void onConnectFailed() {
                    STATE = 0;
                    MODE = -1;
                    LogUtils.DT("客户端与 "+ip+" 建立连接失败");
                }

                @Override
                public void onConnectionClosed() {
                    STATE = 0;
                    MODE = -1;
                    LogUtils.DT("客户端与 "+ip+" 连接关闭");
                }

                @Override
                public void onReceiveMessage(SocketMessage msg) {
                    if(msg.command==0){
                        //文本消息
                        try {
                            EventBus.getDefault().post(new String(msg.datas,"UTF-8"));
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            clientSocketUtils.connect(ip);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra("command",-1);
        switch (command){
            case COMMAND_SEND_LOCAL_IP:
                sendIPWithUDP();
                break;
            case COMMAND_END_LOCAL:
                endCall();
                break;
            case COMMAND_SEND_TEXT:
                String text = intent.getStringExtra("text");
                if(text!=null&&!text.isEmpty()){
                    sendText(text);
                }
                break;
        }
        return START_STICKY;
    }

    private SocketMessage getTextMessage(String text){
        SocketMessage msg = new SocketMessage();
        msg.command=0;
        try {
            msg.datas = text.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    private void sendText(String text) {
        SocketMessage msg = getTextMessage(text);
        if(MODE==0){
            clientSocketUtils.send(msg);
        }else if(MODE==1){
            serverSocketUtils.send(msg);
        }
    }

    private void endCall() {
        if(MODE==0){
            clientSocketUtils.close();
        }else if(MODE==1){
            serverSocketUtils.close();
        }
        STATE=0;
        MODE=-1;
    }

    private void sendIPWithUDP() {
        JSONObject json = new JSONObject();
        try {
            json.put("cmd","broadcast_ip");
            json.put("ip",WifiUtils.getWifiIP(getBaseContext()));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        udpUtils.send(json.toString());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void endCall(Context context) {
        Intent intent = new Intent(context,SocketService.class);
        intent.putExtra("command",COMMAND_END_LOCAL);
        context.startService(intent);
    }

    public static void sendText(Context context ,String s) {
        Intent intent = new Intent(context,SocketService.class);
        intent.putExtra("command",COMMAND_SEND_TEXT);
        intent.putExtra("text",s);
        context.startService(intent);
    }
}
