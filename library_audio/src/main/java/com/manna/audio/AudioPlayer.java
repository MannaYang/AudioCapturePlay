package com.manna.audio;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

/**
 * 封装MediaPlayer播放音频
 */
public class AudioPlayer {

    private final String TAG = AudioPlayer.class.getSimpleName();

    private MediaPlayer mediaPlayer;
    //播放源
    private String dataSource;

    private AudioPlayerListener listener;

    //是否完全播放完成
    private boolean isComplete;


    public AudioPlayerListener getListener() {
        return listener;
    }

    public void setListener(AudioPlayerListener listener) {
        this.listener = listener;
    }

    public AudioPlayer(String dataSource, int sourceType) {
        initPlayer(dataSource, sourceType);
    }

    /**
     * 初始化player
     *
     * @param dataSource :播放源
     * @param sourceType :播放源类型,0 表示本地资源,1 表示网络资源
     */
    private void initPlayer(String dataSource, int sourceType) {
        resetPlayer();

        mediaPlayer = new MediaPlayer();

        // 准备好的监听
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if (listener != null) {
                    listener.onPrepared(mp);
                }
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                isComplete = true;
                if (listener != null)
                    listener.onComplete(mp);
            }
        });

        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                if (listener != null) {
                    listener.onError(mp, what, extra);
                }
                return true;
            }
        });

        try {
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setScreenOnWhilePlaying(true);
            if (sourceType == 0) {
                mediaPlayer.setDataSource(dataSource);
                mediaPlayer.prepare();
            }/* else {
                mediaPlayer.setDataSource(context, Uri.parse("路径地址"));
                mediaPlayer.prepareAsync();
            }*/
            Log.d(TAG, "initPlayer: 初始化MediaPlayer");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "initPlayer: 初始化失败");
            if (listener != null)
                listener.onError(mediaPlayer, MediaPlayer.MEDIA_ERROR_UNKNOWN, 0);
        }
    }

    public void start() {
        if (mediaPlayer.isPlaying()) {
            return;
        }
        mediaPlayer.start();
    }

    public void pause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stop() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            isComplete = false;
        }
    }

    /**
     * 重置player
     */
    private void resetPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.reset();
            mediaPlayer.release();
            isComplete = false;
        }
    }

    //状态
    public boolean isPlaying() {
        if (mediaPlayer == null) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    //跳转播放
    public void seekTo(int progress) {
        if (mediaPlayer != null) {
            mediaPlayer.seekTo(progress);
        }
    }

    //获取时长
    public int getDuration() {
        if (mediaPlayer != null) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    //获取当前播放位置
    public int getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    //获取播放完成状态
    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    /**
     * 状态监听
     */
    public interface AudioPlayerListener {
        void onPrepared(MediaPlayer mp);

        void onComplete(MediaPlayer mp);

        void onError(MediaPlayer mp, int what, int extra);
    }
}
