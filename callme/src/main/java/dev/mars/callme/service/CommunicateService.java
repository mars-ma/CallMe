package dev.mars.callme.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import java.io.UnsupportedEncodingException;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.bean.UDPMessage;
import dev.mars.callme.remote.UDPUtils;
import dev.mars.callme.utils.LogUtils;
import dev.mars.callme.utils.WifiUtils;

public class CommunicateService extends Service {

    private int UDP_PORT=0;
    private int TCP_PORT=0;
    public static final int COMMAND_START = -1;
    public static final int COMMAND_SEND_LOCAL_IP=0;
    public static final int COMMAND_END_LOCAL=1;
    public static final int COMMAND_SEND_TEXT=2;
    public static int STATE = 0; //0:默认 1:服务器端等待通信 2:服务器器端已连接 3:客户端等待通信 4：客户端已连接

    private UDPUtils udpUtils;

    public CommunicateService() {
    }

    public static void startListen(Context context,int udp_port,int tcp_port){
        Intent intent = new Intent(context,CommunicateService.class);
        intent.putExtra("udp_port",udp_port);
        intent.putExtra("tcp_port",tcp_port);
        intent.putExtra("command",COMMAND_START);
        context.startService(intent);
    }

    public static void startCall(Context context){
        Intent intent = new Intent(context,CommunicateService.class);
        intent.putExtra("command",COMMAND_SEND_LOCAL_IP);
        context.startService(intent);
    }

    private void startSocketListen(){
        if(STATE==0) {
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        udpUtils = new UDPUtils(getBaseContext());
    }

    private void startSocketConnect(final String ip){
        if(STATE==0) {
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int command = intent.getIntExtra("command",-1);
        switch (command){
            case COMMAND_START:
                UDP_PORT = intent.getIntExtra("udp_port",5999);
                TCP_PORT = intent.getIntExtra("tcp_port",6000);
                startUDPListen(UDP_PORT);
                break;
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

    private void startUDPListen(int udp_port) {
        udpUtils.setPort(UDP_PORT);
        udpUtils.setOnReceiveListener(new UDPUtils.OnReceiveListener() {
            @Override
            public void onReceive(UDPMessage message) {
                LogUtils.DT("收到UDP command:"+message.getCommand()+" data:"+message.getData());
            }
        });
        udpUtils.listen();
    }

    private SocketMessage getTextMessage(String text){
        SocketMessage msg = new SocketMessage();
        msg.setCommand((byte) 0);
        try {
            msg.setData(text.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return msg;
    }

    private void sendText(String text) {
        SocketMessage msg = getTextMessage(text);
        if(STATE==4){
        }else if(STATE==2){
        }
    }

    private void endCall() {
        if(STATE==4){
        }else if(STATE==2){
        }
        STATE=0;
    }

    private void sendIPWithUDP() {
        UDPMessage message = new UDPMessage();
        message.setMessage(1,WifiUtils.getWifiIP(getBaseContext()));
        udpUtils.send(message);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void endCall(Context context) {
        Intent intent = new Intent(context,CommunicateService.class);
        intent.putExtra("command",COMMAND_END_LOCAL);
        context.startService(intent);
    }

    public static void sendText(Context context ,String s) {
        Intent intent = new Intent(context,CommunicateService.class);
        intent.putExtra("command",COMMAND_SEND_TEXT);
        intent.putExtra("text",s);
        context.startService(intent);
    }
}
