package com.manna.audio;

/**
 * 音乐播放控制接口，与AudioPlayer中方法一致
 */
public interface AudioPlayInterface {

    void start();

    void pause();

    void stop();

    boolean isPlaying();

    boolean isComplete();

    void seekTo(int progress);

    int getDuration();

    int getCurrentPosition();
}
