package dev.mars.callme.remote.mina;

import org.apache.mina.core.filterchain.IoFilterAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.core.write.WriteRequest;

import dev.mars.callme.bean.KeepAliveMessage;
import dev.mars.callme.common.Constants;
import dev.mars.callme.utils.LogUtils;


/**
 * 通过Mina框架实现TCP通讯
 * Created by ma.xuanwei on 2016/12/21.
 */

public class KeepAliveFilter extends IoFilterAdapter{
    private KeepAliveMessage keepAliveMessage = new KeepAliveMessage();

    @Override
    public void messageSent(NextFilter nextFilter, IoSession session, WriteRequest writeRequest) throws Exception {
        if(keepAliveMessage.equals(writeRequest.getMessage())){
            //如果发送的消息是心跳包，拦截该事件
        	LogUtils.DT(session.getId()+" 向服务器发送心跳包");
        }else {
            super.messageSent(nextFilter, session, writeRequest);
        }
    }

    @Override
    public void messageReceived(NextFilter nextFilter, IoSession session, Object message) throws Exception {
        if(keepAliveMessage.equals(message)){
            //如果收到服务器返回的心跳包，拦截该事件
        	LogUtils.DT(session.getId()+" 收到服务器心跳包");
        }else {
            super.messageReceived(nextFilter, session, message);
        }
    }

    @Override
    public void sessionIdle(NextFilter nextFilter, IoSession session, IdleStatus status) throws Exception {
        if(status==IdleStatus.WRITER_IDLE){
            session.write(keepAliveMessage);
        }else if(status==IdleStatus.READER_IDLE&&session.getIdleCount(IdleStatus.READER_IDLE)== Constants.READ_IDLE_CLOSE_TIMES){
            //READ_IDLE_CLOSE_TIMES次Read空闲就关闭session
            session.closeOnFlush();
//            System.out.println(session.getId()+" 未收到服务器心跳包，主动关闭");
            session.setAttribute("reconnect",new Boolean(true));
        }else{
            nextFilter.sessionIdle(session,status);
        }
    }
}
