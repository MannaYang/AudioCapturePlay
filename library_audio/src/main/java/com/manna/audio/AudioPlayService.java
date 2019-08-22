package com.manna.audio;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 播放service
 */
public class AudioPlayService extends Service {
    private final String TAG = AudioPlayService.class.getSimpleName();
    private AudioPlayer audioPlayer;
    //系统音频管理工具
    private AudioManager audioManager;
    //定时发送进度更新
    private Timer timer;
    //标记是否播放完成
    private boolean isComplete;
    //标记是否获得音频焦点
    private boolean isFocusGranted;
    private MediaPlayListener listener;

    public void setListener(MediaPlayListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        isFocusGranted = requestAudioFocus();
        Log.d(TAG, "onCreate: 获取音频焦点" + isFocusGranted);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AudioPlayManager();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (audioPlayer != null)
            audioPlayer.stop();
        if (timer != null)
            timer.cancel();
        if (audioManager != null)
            audioManager.abandonAudioFocus(audioFocusListener);//放弃音频焦点
        listener = null;
        isFocusGranted = false;
    }

    /**
     * 申请获取音频焦点
     */
    private boolean requestAudioFocus() {
        int requestResult = audioManager.requestAudioFocus(audioFocusListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);
        //返回是否授权
        return requestResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    //单独定义listener
    AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                //暂时失去音频焦点,暂停MediaPlayer
                audioPlayer.pause();
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                //获得音频焦点
                audioPlayer.start();
            } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                //失去音频焦点,释放MediaPlayer
                audioManager.abandonAudioFocus(audioFocusListener);
                audioPlayer.stop();
            }
        }
    };

    /**
     * 提供上层调用方法 -- AudioPlayManager
     */
    public class AudioPlayManager extends Binder implements AudioPlayInterface, AudioPlayer.AudioPlayerListener {

        public void initPlayer(String dataSource, int sourceType, MediaPlayListener mediaPlayListener) {
            audioPlayer = new AudioPlayer(dataSource, sourceType);
            audioPlayer.setListener(this);
            setListener(mediaPlayListener);
        }

        @Override
        public void start() {
            //播放完成继续播放 -- 清空完成标记(MediaPlayer并未reset)
            audioPlayer.setComplete(false);
            audioPlayer.start();
            sendPlayProgress();
        }

        @Override
        public void pause() {
            audioPlayer.pause();
            if (listener != null)
                listener.onPause(audioPlayer.getCurrentPosition());
        }

        @Override
        public void stop() {
            audioPlayer.stop();
        }

        @Override
        public boolean isPlaying() {
            return audioPlayer.isPlaying();
        }

        @Override
        public boolean isComplete() {
            return audioPlayer.isComplete();
        }

        @Override
        public void seekTo(int progress) {
            audioPlayer.seekTo(progress);
        }

        @Override
        public int getDuration() {
            return audioPlayer.getDuration();
        }

        @Override
        public int getCurrentPosition() {
            return audioPlayer.getCurrentPosition();
        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            //prepared
            if (listener != null)
                listener.onPrepared(mp);
        }

        @Override
        public void onComplete(MediaPlayer mp) {
            //complete
            if (listener != null)
                listener.onComplete(mp);
        }

        @Override
        public void onError(MediaPlayer mp, int what, int extra) {
            //error
            if (listener != null)
                listener.onError(mp, what, extra);
        }
    }

    /**
     * 开始播放后子线程定时发送进度信息
     */
    private void sendPlayProgress() {
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean isPaused = !audioPlayer.isPlaying() && audioPlayer.getCurrentPosition() > 1;
                if (isPaused) {
                    //播放器处于暂停状态,停止发送进度
                    return;
                }
                if (audioPlayer.isPlaying()) {
                    //正在播放中,回调播放进度
                    int currentPosition = audioPlayer.getCurrentPosition();
                    if (listener != null)
                        listener.onPlaying(currentPosition);
                } else if (audioPlayer.isComplete()) {
                    //播放完成 -- 返回音频最大值
                    int currentPosition = audioPlayer.getDuration();
                    if (listener != null)
                        listener.onPlaying(currentPosition);
                    timer.cancel();
                } else {
                    int currentPosition = 0;
                    if (listener != null)
                        listener.onPlaying(currentPosition);
                    timer.cancel();
                }
            }
        }, 200, 200);
    }

    /**
     * 播放进度监听
     */
    public interface MediaPlayListener {
        //播放中
        void onPlaying(int duration);

        //暂停中
        void onPause(int duration);

        //准备完成
        void onPrepared(MediaPlayer mp);

        //播放完成
        void onComplete(MediaPlayer mp);

        //初始化、播放出错
        void onError(MediaPlayer mp, int what, int extra);
    }
}
