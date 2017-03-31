package dev.mars.audio;

import android.provider.SyncStateContract;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by mars_ma on 2017/3/1audio6.
 */

public class AudioUtils {
    private ExecutorService executor = Executors.newCachedThreadPool();
    private NativeLib nativeBridge;

    public AudioUtils(){
        nativeBridge = new NativeLib();
    }

    public boolean recordAndPlayPCM(final boolean enable1, final boolean enable2){
        if(!nativeBridge.isRecordingAndPlaying()) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    nativeBridge.recordAndPlayPCM(enable1,enable2);
                }
            });
            return true;
        }else{
            return false;
        }
    }

    public boolean stopRecordAndPlay(){
        if(!nativeBridge.isRecordingAndPlaying()) {
            return false;
        }else{
            nativeBridge.stopRecordingAndPlaying();
            return true;
        }
    }

    public void startPlay(final BlockingQueue<AudioFrame> queue){
        if(isPlaying())
            return;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeBridge.startPlay2(queue);
            }
        });
    }


    public void stopRecord(){
        nativeBridge.stopRecording();
    }

    public void startRecord(final OnRecordListener onRecordListener){
        if(nativeBridge.isRecording()){
            return;
        }
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeBridge.setOnRecordListener(new NativeLib.OnRecordListener() {
                    @Override
                    public void onRecord(byte[] datas) {
                        onRecordListener.onRecord(datas);
                    }

                    @Override
                    public void onStart() {
                        onRecordListener.onStart();
                    }
                });
                nativeBridge.startRecording2(Common.SAMPLERATE,Common.PERIOD_TIME,Common.CHANNELS);
            }
        });
    }

    public void stopPlay() {
        nativeBridge.stopPlaying();
        LogUtils.DEBUG("stopPlay");
    }

    public interface OnRecordListener{
        void onRecord(byte[] datas);
        void onStart();
    }

    public boolean isPlaying(){
        return nativeBridge.isPlaying();
    }

    public boolean isRecording(){
        return nativeBridge.isRecording();
    }
}
