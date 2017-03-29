package dev.mars.callme.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.greenrobot.eventbus.EventBus;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.bean.UDPMessage;
import dev.mars.callme.remote.UDPUtils;
import dev.mars.callme.remote.mina.client.ClosedSessionState;
import dev.mars.callme.remote.mina.client.MinaSocketClient;
import dev.mars.callme.remote.mina.server.MinaSocketServer;
import dev.mars.callme.utils.LogUtils;
import dev.mars.callme.utils.WifiUtils;

import static dev.mars.callme.remote.mina.client.ClientSessionStatus.CONNECTED;

public class CommunicateService extends Service {

    private int UDP_PORT=0;
    private int TCP_PORT=0;
    public static final int COMMAND_START = -1;
    public static final int COMMAND_SEND_LOCAL_IP=0;
    public static final int COMMAND_END_LOCAL=1;
    public static final int COMMAND_SEND_TEXT=2;
    public static int STATE = 0; //0:默认 1:服务器端等待通信 2:服务器器端已连接 3:客户端等待通信 4：客户端已连接

    private UDPUtils udpUtils;
    private MinaSocketServer minaSocketServer;
    private MinaSocketClient minaSocketClient;

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
            if(minaSocketServer==null)
                minaSocketServer = new MinaSocketServer();
            minaSocketServer.setPort(TCP_PORT);
            minaSocketServer.setIoHandler(new IoHandlerAdapter(){
                @Override
                public void sessionOpened(IoSession session) throws Exception {
                    super.sessionOpened(session);
                    STATE =2;
                    String clientIP = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();
                    int clientPort =  ((InetSocketAddress)session.getRemoteAddress()).getPort();
                    LogUtils.DT("TCP 服务端与客户端 "+clientIP+" : "+clientPort +" 建立链接");

                    SocketMessage socketMessage = new SocketMessage();
                    socketMessage.setCommand(SocketMessage.COMMAND_SEND_TEXT);
                    socketMessage.setData(("服务端IP:"+WifiUtils.getWifiIP(getApplication())).getBytes("UTF-8"));
                    minaSocketServer.send(socketMessage,null);
                }

                @Override
                public void sessionClosed(IoSession session) throws Exception {
                    super.sessionClosed(session);
                    STATE = 0;
                }

                @Override
                public void messageReceived(IoSession session, Object message) throws Exception {
                    super.messageReceived(session, message);
                    SocketMessage socketMessage = (SocketMessage)message;
                    LogUtils.DT("TCP 服务端收到消息 command:"+socketMessage.getCommand());
                    switch (socketMessage.getCommand()){
                        case SocketMessage.COMMAND_SEND_TEXT:
                            EventBus.getDefault().post(new String(socketMessage.getData(),"UTF-8"));
                            break;
                    }
                }

                @Override
                public void messageSent(IoSession session, Object message) throws Exception {
                    super.messageSent(session, message);
                    SocketMessage socketMessage = (SocketMessage)message;
                    LogUtils.DT("TCP 服务端发送消息 command:"+socketMessage.getCommand());
                }
            });
            minaSocketServer.bind(new MinaSocketServer.OnBindListener() {
                @Override
                public void onBindSucess() {
                    STATE = 1;
                    LogUtils.DT("TCP 服务端已绑定端口 "+minaSocketServer.getPort());
                }

                @Override
                public void onBindFailed() {
                    STATE = 0;
                    LogUtils.DT("TCP 服务端绑定端口失败 "+minaSocketServer.getPort());
                }
            });

        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        udpUtils = new UDPUtils(getBaseContext());
    }

    private void startSocketConnect(final String ip){
        if(STATE==0) {

            if(minaSocketClient==null)
                minaSocketClient = new MinaSocketClient();
            minaSocketClient.setIoHandler(new IoHandlerAdapter(){
                @Override
                public void sessionOpened(IoSession session) throws Exception {
                    super.sessionOpened(session);
                    STATE = 4;
                    String serverIP = ((InetSocketAddress)session.getRemoteAddress()).getAddress().getHostAddress();
                    int serverPort =  ((InetSocketAddress)session.getRemoteAddress()).getPort();
                    LogUtils.DT("TCP 客户端与服务端 "+serverIP+" : "+serverPort +" 建立链接");

                    //test
                    SocketMessage socketMessage = new SocketMessage();
                    socketMessage.setCommand(SocketMessage.COMMAND_SEND_TEXT);
                    socketMessage.setData(("客户端IP:"+WifiUtils.getWifiIP(getApplication())).getBytes("UTF-8"));
                    minaSocketClient.send(socketMessage,null,false);
                }

                @Override
                public void sessionClosed(IoSession session) throws Exception {
                    super.sessionClosed(session);
                    STATE = 0;
                    LogUtils.DT("TCP 客户端会话关闭");
                }

                @Override
                public void messageReceived(IoSession session, Object message) throws Exception {
                    super.messageReceived(session, message);
                    SocketMessage socketMessage = (SocketMessage)message;
                    LogUtils.DT("TCP 客户端收到消息 command:"+socketMessage.getCommand());
                    switch (socketMessage.getCommand()){
                        case SocketMessage.COMMAND_SEND_TEXT:
                            EventBus.getDefault().post(new String(socketMessage.getData(),"UTF-8"));
                            break;
                    }
                }

                @Override
                public void messageSent(IoSession session, Object message) throws Exception {
                    super.messageSent(session, message);
                    SocketMessage socketMessage = (SocketMessage)message;
                    LogUtils.DT("TCP 客户端发送消息 command:"+socketMessage.getCommand());
                }

                @Override
                public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
                    super.exceptionCaught(session, cause);
                    LogUtils.DT("TCP 客户端异常:"+cause.getMessage());
                    minaSocketClient.closeSession();
                }
            });
            minaSocketClient.setPort(TCP_PORT);
            minaSocketClient.setIP(ip);
            STATE = 3;
            minaSocketClient.connect(new IoFutureListener<ConnectFuture>() {
                @Override
                public void operationComplete(ConnectFuture connectFuture) {
                    if (!connectFuture.isConnected() || connectFuture.isCanceled()) {
                        STATE = 0;
                        LogUtils.DT("TCP 客户端链接失败");
                    }
                }
            });
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



    private SocketMessage getTextMessage(String text){
        SocketMessage msg = new SocketMessage();
        msg.setCommand(SocketMessage.COMMAND_SEND_TEXT);
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
            minaSocketClient.send(msg,null,false);
        }else if(STATE==2){
            minaSocketServer.send(msg,null);
        }
    }

    private void endCall() {
        if(STATE==4){
            minaSocketClient.closeSession();
        }else if(STATE==2){
            minaSocketServer.closeSession();
            minaSocketServer.unbind();
        }
        STATE=0;
    }

    private void startUDPListen(int udp_port) {
        udpUtils.setPort(UDP_PORT);
        udpUtils.setOnReceiveListener(new UDPUtils.OnReceiveListener() {
            @Override
            public void onReceive(UDPMessage message) {

                if(WifiUtils.getWifiIP(getApplication()).equals(message.getData())){
                    return;
                }
                LogUtils.DT("收到UDP command:"+message.getCommand()+" data:"+message.getData());
                switch (message.getCommand()){
                    case UDPMessage.COMMAND_FIND_OTHER:
                        //收到局域网内另一终端的广播，告诉它开始TCP通信
                        String destIP = message.getData();
                        UDPMessage tcpConnectMsg = new UDPMessage();
                        tcpConnectMsg.setMessage(UDPMessage.COMMAND_START_TCP_CONNECTION,WifiUtils.getWifiIP(getApplication()));
                        //监听TCP端口
                        startSocketListen();
                        udpUtils.send(tcpConnectMsg);
                        break;
                    case UDPMessage.COMMAND_START_TCP_CONNECTION:
                        //收到局域网内另一终端的TCP通讯请求
                        String destIP2 = message.getData();
                        startSocketConnect(destIP2);
                        break;
                }
            }
        });
        udpUtils.listen();
    }

    private void sendIPWithUDP() {
        UDPMessage message = new UDPMessage();
        message.setMessage(UDPMessage.COMMAND_FIND_OTHER,WifiUtils.getWifiIP(getBaseContext()));
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

    @Override
    public void onDestroy() {
        super.onDestroy();
        endCall();
    }
}
