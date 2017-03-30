package dev.mars.callme.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Socket通信上层消息的基类
 * |0x5c|0x74|bodylength|command|data|
 * @author mars
 *
 */
public class SocketMessage implements Serializable{

	public static final byte COMMAND_SEND_TEXT= 1;
	public static final byte COMMAND_SEND_HEART_BEAT= 0;
	public static final byte COMMAND_REQUEST_CALL= 2;
	public static final byte COMMAND_REPONSE_CALL_OK= 3; //同意通话
	public static final byte COMMAND_REPONSE_CALL_REFUSE= 4; //拒绝通话

	public static final byte HEADER1 = 0x5c;
	public static final byte HEADER2 = 0x74;
	private byte command; // 0：心跳包  1:发送文字
	private byte[] data;



	/**
	 * 设置消息体，一般用json解析
	 */
	public void setData(byte[] d) {
		this.data = d;
	}

	public void setCommand(byte b){
		command = b;
	}

	public byte getCommand(){
		return command;
	}

	public byte[] getData(){
		return data;
	}

}
