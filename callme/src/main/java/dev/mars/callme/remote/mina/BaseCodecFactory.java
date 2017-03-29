package dev.mars.callme.remote.mina;

import java.nio.charset.Charset;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class BaseCodecFactory implements ProtocolCodecFactory {
	BaseEncoder encoder;
	BaseDecoder decoder;
	
	public BaseCodecFactory() {
		// TODO Auto-generated constructor stub
		encoder = new BaseEncoder();
		decoder = new BaseDecoder();
	}
	

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		return encoder;
	}

}
