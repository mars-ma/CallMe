package dev.mars.callme.remote;

import dev.mars.callme.bean.SocketMessage;

/**
 * Created by ma.xuanwei on 2017/3/27.
 */

public interface IOHandler {
    void onConnected();
    void onConnectFailed();
    void onConnectionClosed();
    void onReceiveMessage(SocketMessage msg);
}
