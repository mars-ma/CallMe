package dev.mars.callme.remote.mina;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.Charset;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.utils.BasicTypeConvertUtils;


public class BaseDecoder extends CumulativeProtocolDecoder {



    public BaseDecoder() {
    }

    @Override
    protected boolean doDecode(IoSession session, IoBuffer in,
                               ProtocolDecoderOutput out) throws Exception {
        // TODO Auto-generated method stub
        if (in.remaining() >= 4) {
//			System.out.println("1.缓冲区目前数组长度:"+in.remaining());
            //当可读的缓冲区长度大于4时（前两个字节是占位符，后两个字节是长度）
            in.mark(); // 标记当前位置，方便reset
            byte[] header = new byte[4];
            in.get(header, 0, header.length);
//			System.out.println("receive header[0]:"+header[0]+"|header[1]:"+header[1]);
            if (header[0] == SocketMessage.HEADER1 && header[1] == SocketMessage.HEADER2) {
//				System.out.println("header[2]:"+header[2]+",header[3]:"+header[3]);
                short bodyLength = BasicTypeConvertUtils.byteToShort(header[2], header[3]);
//				System.out.println("报文内容长度:"+bodyLength);
//				System.out.println("2.缓冲区目前数组长度:"+in.remaining());

                if (in.remaining() >= bodyLength) {
                    //可读取完整的报文
//					System.out.println(in.remaining()>=bodyLength);
                    SocketMessage msg = new SocketMessage();
                    byte command = in.get();
                    msg.setCommand(command);
                    if(command!=-1) { //如果不是心跳包
                        byte[] data = new byte[bodyLength - 1];
                        in.get(data, 0, bodyLength);
                        msg.setData(data);
                        out.write(msg);
                    }
//						System.out.println("3.缓冲区目前数组长度:" + in.remaining())
                    if (in.remaining() > 0) {
//						System.out.println("粘包，保留未消费数据");
                        return true;
                    }
//					System.out.println("不粘包，IoBuffer中的数据已消费");
                } else {
//					System.out.println("缓冲区未接收完全应用层报文，继续读取字节流");
                    in.reset();
                    return false;
                }

            }

        }
        return false;
    }


}
