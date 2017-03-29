package dev.mars.callme.remote.mina.server;

import android.os.Process;

import org.apache.mina.core.future.IoFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.future.WriteFuture;
import org.apache.mina.core.service.IoHandler;
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
    private SocketAcceptor acceptor;
    //监听端口
    private int PORT;
    //与客户端的唯一会话
    private IoSession tcpSession;
    private IoHandler mIoHandler = new IoHandler() {
        @Override
        public void sessionCreated(IoSession ioSession) throws Exception {

        }

        @Override
        public void sessionOpened(IoSession ioSession) throws Exception {
            tcpSession = ioSession;
            unbind();
            LogUtils.DT("已建立TCP Session");
        }

        @Override
        public void sessionClosed(IoSession ioSession) throws Exception {
            tcpSession = null;
            LogUtils.DT("TCP Session 关闭");
        }

        @Override
        public void sessionIdle(IoSession ioSession, IdleStatus idleStatus) throws Exception {

        }

        @Override
        public void exceptionCaught(IoSession ioSession, Throwable throwable) throws Exception {
            if (tcpSession != null) {
                tcpSession.closeOnFlush();
            }
        }

        @Override
        public void messageReceived(IoSession ioSession, Object o) throws Exception {

        }

        @Override
        public void messageSent(IoSession ioSession, Object o) throws Exception {

        }

        @Override
        public void inputClosed(IoSession ioSession) throws Exception {

        }
    };

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
        acceptor.getFilterChain().addLast("KeepAlive", new KeepAliveFilter());
        acceptor.setHandler(mIoHandler);
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

    public void bind() {
        if (acceptor.isActive()) {
            return;
        }
        service.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    acceptor.bind(new InetSocketAddress(getPort()));
                    LogUtils.DT("TCP Server 正在监听 PORT:" + PORT);
                } catch (IOException e) {
                    e.printStackTrace();
                    LogUtils.E(e.getMessage());
                }
            }
        });
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

}
