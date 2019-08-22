package com.manna.capture.capture;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.LinearInterpolator;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.manna.audio.AudioPlayService;
import com.manna.capture.R;
import com.manna.audio.utils.TimeUtils;

/**
 * 音乐播放
 */
public class AudioPlayActivity extends AppCompatActivity implements AudioPlayService.MediaPlayListener {

    private TextView tvMusicName, tvTotalLength;
    private ImageView /*ivPlayDisc,*/ ivPlayNeedle, ivPlayDiscRound;
    private Chronometer tvAudioTime;
    private SeekBar sbPlay;
    private ImageView ivPlay, ivMode;
    private String filePath;

    private ObjectAnimator discAnimator, needleAnimator;

    private boolean isStartPlay = false;
    private long currentPlayTime;
    private AudioPlayService.AudioPlayManager audioPlayManager;

    // 记录总时间
    private long recordingTime = 0;
    //记录是否重复单曲
    private boolean isRepeat = false;
    //AudioPlayService
    private Intent intent;
    private ServiceConnection connection;
    //标记是否手指拖动SeekBar
    private boolean isThumb;
    //更新进度
    Handler handler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_play);
        initPlayService();

        initView();
        initData();
    }

    private void initView() {
        tvMusicName = findViewById(R.id.tv_music_name);
        ivPlayNeedle = findViewById(R.id.iv_play_needle);
        ivPlayDiscRound = findViewById(R.id.iv_play_disc_round);
        setRoundDrawable(ivPlayDiscRound);

        ivPlayNeedle.setPivotX(0);
        ivPlayNeedle.setPivotY(0);

        sbPlay = findViewById(R.id.sb_play);
        tvAudioTime = findViewById(R.id.tv_audio_time);
        tvTotalLength = findViewById(R.id.tv_total_length);
        ivPlay = findViewById(R.id.iv_play);
        ivMode = findViewById(R.id.iv_mode);

        ivPlay.setOnClickListener(v -> {
            if (!isStartPlay) {
                //状态切换
                changePlayStatus(true);
                //唱片动画
                startDiscAnimator();
                //唱针动画
                startNeedleAnimator(false);
                //开始计时
                startAudioTime();
                //MediaPlayer
                audioPlayManager.start();
            } else {
                //点击暂停
                changePlayStatus(false);
                //暂停时获取动画当前播放时间,下次继续从当前位置播放
                pauseDiscAnimator(false);
                //暂停时恢复唱针位置
                startNeedleAnimator(true);
                //停止计时
                pauseAudioTime(false);
                //MediaPlayer
                audioPlayManager.pause();
            }
        });

        ivMode.setOnClickListener(v -> {
            if (isRepeat) {
                //单曲循环 -- 单曲播放
                ivMode.setImageResource(R.mipmap.ic_play_single);
                isRepeat = false;
            } else {
                //单曲播放 -- 单曲循环
                ivMode.setImageResource(R.mipmap.ic_play_round);
                isRepeat = true;
            }
        });

        sbPlay.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //progress change
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isThumb = true;
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isThumb = false;
                audioPlayManager.seekTo(seekBar.getProgress());
                //拖动SeekBar对应更新计时器
                recordingTime = seekBar.getProgress();
                startAudioTime();
            }
        });
    }

    private void initData() {
        //此处简写,应传递Bundle
        filePath = getIntent().getStringExtra("filePath");
        String fileName = getIntent().getStringExtra("fileName");
        tvMusicName.setText(fileName);
        sbPlay.getThumb().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        sbPlay.getProgressDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);

        initDiscAnimator();
        //xml中沿Z轴旋转-30,所以起始位置为-30
        initNeedleAnimator(-30, 0);
    }

    private void initPlayService() {
        intent = new Intent(this, AudioPlayService.class);
        startService(intent);
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                audioPlayManager = (AudioPlayService.AudioPlayManager) service;
                initPlayer();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private void initPlayer() {
        //同步Prepared，异步实现需启用OnPreparedListener
        audioPlayManager.initPlayer(filePath, 0, this);
    }

    /**
     * 唱片旋转
     */
    private void initDiscAnimator() {
        discAnimator = ObjectAnimator.ofFloat(ivPlayDiscRound, "rotation", 0, 360);
        discAnimator.setDuration(20000);
        //使ObjectAnimator动画匀速平滑旋转
        discAnimator.setInterpolator(new LinearInterpolator());
        //无限循环旋转
        discAnimator.setRepeatCount(ValueAnimator.INFINITE);
        discAnimator.setRepeatMode(ValueAnimator.RESTART);
    }

    /**
     * 唱针旋转
     *
     * @param startOffset :起始位置
     * @param rotation    ：绕Z轴旋转角度
     */
    private void initNeedleAnimator(float startOffset, float rotation) {
        needleAnimator = ObjectAnimator.ofFloat(ivPlayNeedle, "rotation", startOffset, rotation);
        needleAnimator.setDuration(1000);
        ivPlayNeedle.setPivotX(0);
        ivPlayNeedle.setPivotY(0);
        needleAnimator.setInterpolator(new LinearInterpolator());
    }

    /**
     * 原生设置圆角
     *
     * @param img :image view
     */
    private void setRoundDrawable(ImageView img) {
        RoundedBitmapDrawable shopDrawable = RoundedBitmapDrawableFactory.create(
                getResources(), BitmapFactory.decodeResource(getResources(), R.mipmap.ic_round));
        shopDrawable.setCircular(true);
        img.setBackground(shopDrawable);
    }

    /**
     * 切换播放状态
     *
     * @param isPlay true 表示开始播放,false表示停止播放
     */
    private void changePlayStatus(boolean isPlay) {
        isStartPlay = isPlay;
        if (isPlay) {
            ivPlay.setImageResource(R.mipmap.ic_pause_record);
        } else {
            ivPlay.setImageResource(R.mipmap.ic_play);
        }
    }

    /**
     * 设置计时器时间
     */
    private void startAudioTime() {
        int hour;
        tvAudioTime.setBase(SystemClock.elapsedRealtime() - recordingTime);
        hour = (int) ((SystemClock.elapsedRealtime() - tvAudioTime.getBase()) / 1000 / 1000 / 60);
        String timeStr;
        if (hour < 10) {
            timeStr = "0" + hour + ":%s";
        } else {
            timeStr = hour + ":%s";
        }
        tvAudioTime.setFormat(timeStr);
        tvAudioTime.start();
    }

    /**
     * 暂停 -- 停止计时
     *
     * @param isStop : true 表示停止计时,false 表示暂停
     */
    private void pauseAudioTime(boolean isStop) {
        if (isStop) {
            recordingTime = 0;
            //重置为00:00:00
            tvAudioTime.setBase(SystemClock.elapsedRealtime());
        } else {
            recordingTime = SystemClock.elapsedRealtime() - tvAudioTime.getBase();
        }
        tvAudioTime.stop();
    }

    /**
     * 开始旋转唱片动画
     */
    private void startDiscAnimator() {
        discAnimator.setCurrentPlayTime(currentPlayTime);
        discAnimator.start();
    }

    /**
     * 暂停 -- 停止动画
     *
     * @param isStop ：true 停止动画,false 暂停动画
     */
    private void pauseDiscAnimator(boolean isStop) {
        if (isStop) {
            currentPlayTime = 0;
        } else {
            currentPlayTime = discAnimator.getCurrentPlayTime();
        }
        discAnimator.cancel();
    }

    /**
     * 开始旋转唱针动画
     *
     * @param isReBack : true 表示恢复原始位置,false表示唱针开始击打唱片
     */
    private void startNeedleAnimator(boolean isReBack) {
        //记录唱针位置动画
        int startOffset;
        int rotation;
        if (isReBack) {
            //恢复原始位置
            startOffset = 0;
            rotation = -30;
        } else {
            //唱针开始击打唱片
            startOffset = -30;
            rotation = 0;
        }
        initNeedleAnimator(startOffset, rotation);
        needleAnimator.start();
    }

    @Override
    public void onPlaying(int duration) {
        //播放中,更新SeekBar
        handler.post(() -> {
            if (audioPlayManager.isComplete()) {
                sbPlay.setProgress(0);
            } else {
                if (!isThumb) {
                    sbPlay.setProgress(duration);
                }
            }
        });
    }

    @Override
    public void onPause(int duration) {
        //暂停中
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        //播放器准备完毕 do something
        int totalLength = audioPlayManager.getDuration();
        //换算getDuration返回的milliseconds
        String timeStr = TimeUtils.secondToTime(totalLength / 1000);
        tvTotalLength.setText(timeStr);
        sbPlay.setMax(totalLength);
        sbPlay.setProgress(audioPlayManager.getCurrentPosition());
    }

    @Override
    public void onComplete(MediaPlayer mp) {
        //计时清零
        pauseAudioTime(true);
        sbPlay.setProgress(0);
        if (isRepeat) {
            //单曲循环
            changePlayStatus(true);
            //开始唱针动画 -- repeat模式下动画一直持续
            //startNeedleAnimator(false);
            //开始唱片动画 -- repeat模式下动画一直持续
            //startDiscAnimator();
            //MediaPlayer
            audioPlayManager.start();
            //开始计时器
            startAudioTime();
        } else {
            //播放完成,停止继续播放
            changePlayStatus(false);
            //恢复原始唱针位置 --
            startNeedleAnimator(true);
            //停止唱片旋转动画
            pauseDiscAnimator(true);
        }
    }

    @Override
    public void onError(MediaPlayer mp, int what, int extra) {
        //播放出错,停止继续播放
        changePlayStatus(false);
        //恢复原始唱针位置 --
        startNeedleAnimator(true);
        //停止唱片旋转动画
        pauseDiscAnimator(true);
        //计时清零
        pauseAudioTime(true);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //可一直后台播放,在通知栏提供停止播放操作(此处停止Service后台播放)
        unbindService(connection);
        stopService(intent);
    }
}
