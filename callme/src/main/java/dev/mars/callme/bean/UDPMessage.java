package dev.mars.callme.bean;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;


public class UDPMessage implements Serializable{
	int command;
	String data;
	public void setMessage(int c,String s){
		command = c;
		data = s;
	}

	public void setJSON(String str){
		try {
			JSONObject json  = new JSONObject(str);
			command = json.getInt("command");
			data = json.getString("data");
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public int getCommand(){
		return command;
	}

	public String getData(){
		return data;
	}

	public String toString(){
		JSONObject json = new JSONObject();
		try {
			json.put("command",command);
			json.put("data",data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json.toString();
	}

}
