package dev.mars.openslesdemo;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ma.xuanwei on 2017/3/7.
 */

public class NativeLib {

    private AtomicBoolean isRecording = new AtomicBoolean(false);
    private AtomicBoolean isPlaying = new AtomicBoolean(false);
    private AtomicBoolean isRecordAndPlay=new AtomicBoolean(false);
    static {
        System.loadLibrary("native");
    }

    public void setIsRecording(boolean v){
        isRecording.set(v);
        LogUtils.DEBUG("setIsRecording "+v);
    }

    public void setIsRecordingAndPlaying(boolean v){
        isRecordAndPlay.set(v);
        LogUtils.DEBUG("setIsRecordingAndPlaying "+v);
    }

    public boolean isRecording(){
        return isRecording.get();
    }

    public void setIsPlaying(boolean b){
        isPlaying.set(b);
    }

    public boolean isPlaying(){
        return isPlaying.get();
    }

    public boolean isRecordingAndPlaying(){
        return isRecordAndPlay.get();
    }

    public native void startRecording(int sampleRate, int period, int channels, String path);
    public native void stopRecording();
    public native void playRecording(int sampleRate, int period, int channels, String path);
    public native void stopPlaying();
    public native int encode(String pcm,String speex);
    public native int decode(String speex,String pcm);

    public native int recordAndPlayPCM(boolean enableProcess,boolean enableEchoCancel);

    public native int stopRecordingAndPlaying() ;

    public native void startRecording2(int sampleRate, int period, int channels);

    public void onRecord(byte[] bytes){
        LogUtils.DEBUG("收到native传来的bytes，长度 = "+(bytes==null?0:bytes.length));
        if(onRecordListener!=null){
            onRecordListener.onRecord(bytes);
        }
    }
    OnRecordListener onRecordListener;
    public void setOnRecordListener(OnRecordListener l){
        onRecordListener = l;
    }

    public interface OnRecordListener{
        void onRecord(byte[] datas);
    }
}
