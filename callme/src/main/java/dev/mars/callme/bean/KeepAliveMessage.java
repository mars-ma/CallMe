package dev.mars.callme.bean;


import android.text.TextUtils;

/**
 * 心跳包消息
 * body:{ "type":"0"}
 * @author ma.xuanwei
 *
 */
public class KeepAliveMessage extends SocketMessage {
    public KeepAliveMessage(){
        setCommand((byte) -1);
    }

    @Override
    public boolean equals(Object o) {
        try{
            SocketMessage other = (SocketMessage)o;
            return other.getCommand()==-1;
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }
}
