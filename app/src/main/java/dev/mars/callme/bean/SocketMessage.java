package dev.mars.callme.bean;

import java.io.Serializable;

/**
 * Created by ma.xuanwei on 2017/3/23.
 */

public class SocketMessage implements Serializable{
    public static final int COMMAND_START=0;
    public int command;
    public byte[] datas;
}
