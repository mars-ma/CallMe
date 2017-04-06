package dev.mars.callme.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.SyncStateContract;

import dev.mars.callme.common.Constants;

/**
 * Created by mars_ma on 2017/4/5.
 */

public class RingtonePlayer {
    private static MediaPlayer mMediaPlayer;
    private static Vibrator vibrator;

    public static void play(Context context) {
        if(!Constants.RING_TONE_PLAY)
            return;
        //-开始播放手机铃声及震动
        close();
        try {
            Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
            mMediaPlayer = new MediaPlayer();
            mMediaPlayer.setDataSource(context, alert);
//final AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_RING);
            mMediaPlayer.setLooping(true);
            mMediaPlayer.prepare();
            mMediaPlayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            long[] pattern = {800, 150, 400, 130}; // OFF/ON/OFF/ON...
            vibrator.vibrate(pattern, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void close() {
        if(!Constants.RING_TONE_PLAY)
            return;
        try {
            if (mMediaPlayer != null) {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (null != vibrator) {
                vibrator.cancel();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMediaPlayer = null;
        vibrator = null;
    }
}
