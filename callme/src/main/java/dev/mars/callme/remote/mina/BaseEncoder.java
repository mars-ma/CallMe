package dev.mars.callme.remote.mina;

import android.text.TextUtils;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import dev.mars.callme.bean.SocketMessage;

public class BaseEncoder extends ProtocolEncoderAdapter{
	
	public BaseEncoder() {
	}
	

	@Override
	public void encode(IoSession session, Object obj, ProtocolEncoderOutput output)
			throws Exception {
		SocketMessage msg = (SocketMessage) obj;
		IoBuffer buffer = IoBuffer.allocate(100).setAutoExpand(true);
		buffer.order(ByteOrder.BIG_ENDIAN);
		//put head
		buffer.put(SocketMessage.HEADER1);
		buffer.put(SocketMessage.HEADER2);
		buffer.put(msg.getCommand());
		short bodyLength = 1;
		if(msg.getCommand()==-1){
			//发送的是心跳包
			buffer.putShort(bodyLength);
		}else {
			byte[] data = msg.getData();
			bodyLength += (short) data.length;
			buffer.putShort(bodyLength);
			buffer.put(data);
		}
//		System.out.println("HEADER:"+ SocketMessage.HEADER1+"|"+ SocketMessage.HEADER2);
//		System.out.println("Length:"+bodyLength);
//		System.out.println("will send :"+buffer.toString());
		buffer.flip(); 		
		output.write(buffer); 
		
	}



}
