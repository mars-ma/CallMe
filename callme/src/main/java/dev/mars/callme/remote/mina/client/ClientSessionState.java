package dev.mars.callme.remote.mina.client;

import org.apache.mina.core.future.IoFuture;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.remote.mina.ISendListener;


/**
 * 状态模式上层抽象
 * Created by ma.xuanwei on 2017/1/4.
 */

public abstract class ClientSessionState {

    MinaSocketClient minaSocketClient;

    ClientSessionState(MinaSocketClient client){
        minaSocketClient = client;
    }

    /**
     * 关闭连接
     */
    public abstract IoFuture closeConnection();

    /**
     * 请求连接
     */
    public abstract IoFuture connect();

    /**
     * 发送消息
     *
     * @param msg
     * @param listener
     * @param tryConnect 是否在无连接状态下请求连接
     */
    public abstract void send(SocketMessage msg, final ISendListener listener, boolean tryConnect);
}
