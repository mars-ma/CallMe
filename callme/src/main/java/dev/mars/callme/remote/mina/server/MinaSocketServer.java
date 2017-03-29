package dev.mars.callme.remote.mina.server;

import android.os.Process;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.SocketAcceptor;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.mars.callme.bean.SocketMessage;
import dev.mars.callme.common.Constants;
import dev.mars.callme.remote.mina.BaseCodecFactory;
import dev.mars.callme.remote.mina.ISendListener;
import dev.mars.callme.remote.mina.KeepAliveFilter;
import dev.mars.callme.utils.LogUtils;


/**
 * MinaSocket Created by ma.xuanwei on 2016/12/13.
 */

public class MinaSocketServer {
    private ExecutorService service = Executors.newCachedThreadPool();
    IoAcceptor acceptor;
    //监听端口
    private int PORT;
    //与客户端的唯一会话
    private IoSession tcpSession;


    public MinaSocketServer() {
        super();
        init();
    }

    /**
     * 初始化
     */
    private void init() {
        acceptor = new NioSocketAcceptor();
        acceptor.getSessionConfig().setIdleTime(IdleStatus.READER_IDLE,
                Constants.READ_IDLE_TIMEOUT);
        acceptor.getSessionConfig().setIdleTime(IdleStatus.WRITER_IDLE,
                Constants.WRITE_IDLE_TIMEOUT);
        acceptor.getFilterChain().addLast(
                "BaseFilter",
                new ProtocolCodecFilter(new BaseCodecFactory()));
        //acceptor.getFilterChain().addLast("KeepAlive", new KeepAliveFilter());
        acceptor.getSessionConfig().setReadBufferSize(2048);
        acceptor.setHandler(new IoHandlerAdapter(){
            @Override
            public void sessionOpened(IoSession session) throws Exception {
                super.sessionOpened(session);
                LogUtils.DT("已建立TCP Session");
                if(tcpSession!=null&&tcpSession.isConnected()){
                    return;
                }
                tcpSession = session;
                if(ioHandler!=null)
                    ioHandler.sessionOpened(session);
            }

            @Override
            public void sessionClosed(IoSession session) throws Exception {
                super.sessionClosed(session);

                tcpSession = null;
                LogUtils.DT("TCP Session 关闭");
                if(ioHandler!=null)
                    ioHandler.sessionClosed(session);
                unbind();
            }

            @Override
            public void messageReceived(IoSession session, Object message) throws Exception {
                super.messageReceived(session, message);
                if(ioHandler!=null)
                    ioHandler.messageReceived(session,message);
            }

            @Override
            public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
                super.exceptionCaught(session, cause);
                LogUtils.DT("TCP 服务端异常:"+cause.getMessage());
                if (tcpSession != null) {
                    tcpSession.closeOnFlush();
                }
            }
        });
    }

    public int getPort() {
        return PORT;
    }

    /**
     * 设置端口号
     *
     * @param port
     */
    public void setPort(int port) {
        PORT = port;
    }

    public void bind(final OnBindListener listener) {
        if (acceptor.isActive()) {
            return;
        }
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    acceptor.bind(new InetSocketAddress(getPort()));
                    LogUtils.DT("TCP Server 正在监听 PORT:" + PORT);
                    if(listener!=null){
                        listener.onBindSucess();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.E(e.getMessage());
                    if(listener!=null){
                        listener.onBindFailed();
                    }
                }
            }
        });
    }

    public interface OnBindListener{
        void onBindSucess();
        void onBindFailed();
    }

    public void unbind() {
        acceptor.unbind();
        LogUtils.DT("TCP Server 停止监听 PORT:" + PORT);
    }

    /**
     * 发送
     *
     * @param msg
     */
    public void send(final SocketMessage msg, final ISendListener listener) {
        if (tcpSession != null && tcpSession.isConnected()) {
            WriteFuture writeFuture = tcpSession.write(msg);
            writeFuture.addListener(new IoFutureListener<WriteFuture>() {
                @Override
                public void operationComplete(WriteFuture ioFuture) {
                    if (listener != null) {
                        if (ioFuture.isWritten()) {
                            listener.onSendSuccess();
                        } else {
                            listener.onSendFailed("发送失败");
                        }
                    }
                }
            });
        }
    }

    /**
     * 关闭连接
     */
    public void closeSession() {
        if (tcpSession != null && tcpSession.isConnected()) {
            tcpSession.closeOnFlush();
            tcpSession = null;
        }
    }

    private IoHandler ioHandler;
    public void setIoHandler(IoHandler handler){
        ioHandler = handler;
    }

}
