package com.manna.capture.capture;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.manna.audio.AudioCapture;
import com.manna.audio.LameEncode;
import com.manna.audio.utils.FftFactory;
import com.manna.audio.utils.FileUtils;
import com.manna.audio.widget.WaveView;
import com.manna.capture.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 录制原始PCM音频
 */
public class AudioCaptureActivity extends AppCompatActivity implements AudioCapture.AudioCaptureListener {

    private final String TAG = AudioCaptureActivity.class.getSimpleName();

    private ImageView ivStart, ivStop, ivDetail;
    private TextView tvDB, tvStop, tvStart, tvDetail, tvTitle;
    //计时器
    private Chronometer tvAudioTime;
    //频谱
    private WaveView dialView;
    //文件流
    private FileOutputStream fileOutputStream;
    //mp3_buff
    private byte[] mp3_buff;
    //AudioCapture
    private AudioCapture audioCapture;
    private String filePath;
    //标记是否开始录音
    private boolean isStartAudio = false;
    //FFT格式化pcm数据
    private FftFactory fftFactory;
    //Handler此处暂时简写
    private Handler formatHandle = new Handler();
    private Handler decibelHandle = new Handler();

    // 记录总时间
    private long recordingTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_capture);

        initView();
        initData();
    }

    private void initView() {
        ivStart = findViewById(R.id.iv_start);
        tvStart = findViewById(R.id.tv_start);
        tvTitle = findViewById(R.id.tv_title);

        tvStop = findViewById(R.id.tv_stop);
        ivStop = findViewById(R.id.iv_stop);
        ivStop.setEnabled(false);
        ivStop.setAlpha(0.4f);
        tvStop.setAlpha(0.4f);

        ivDetail = findViewById(R.id.iv_detail);
        tvDetail = findViewById(R.id.tv_detail);

        dialView = findViewById(R.id.dial_view);
        tvDB = findViewById(R.id.tv_db);
        tvAudioTime = findViewById(R.id.tv_audio_time);

        ivStart.setOnClickListener(v -> {
            //开始
            String str = tvStart.getText().toString();
            initStatus();
            if (!isStartAudio && str.equals("开始") || str.equals("继续")) {
                // -- 点击开始 --
                filePath = getFilePath();
                initFileStream(filePath);

                isStartAudio = true;
                audioCapture.start();

                tvStart.setText("暂停");
                ivStart.setImageResource(R.mipmap.ic_pause_record);
                //设置计时器时间
                setAudioTime();
                tvAudioTime.start();
            } else {
                // -- 点击暂停 --
                isStartAudio = false;
                audioCapture.stop();

                tvAudioTime.stop();
                recordingTime = SystemClock.elapsedRealtime() - tvAudioTime.getBase();

                tvStart.setText("继续");
                ivStart.setImageResource(R.mipmap.ic_start_record);
            }
        });

        ivStop.setOnClickListener(v -> {
            // -- 点击完成 --
            if (isStartAudio) {
                isStartAudio = false;
                audioCapture.stop();
                writeFlush();
            }
            tvAudioTime.stop();
            resetStatus();
            //录制完成重置输出流
            filePath = getFilePath();
        });

        ivDetail.setOnClickListener(v -> {
            //音频录制列表
            startActivity(new Intent(this, AudioFileActivity.class)
                    .putExtra("dirFilePath", FileUtils.getDiskCachePath(this)));
        });
    }

    private void initData() {
        fftFactory = new FftFactory(FftFactory.Level.Original);
        filePath = getFilePath();
        // calculate mp3buf_size in bytes = 1.25*num_samples + 7200 --  defined in lame.h 697 line
        //lame编码器中的MP3——buffer计算公式,定义在lame.h头文件中
        mp3_buff = new byte[(int) ((int) (7200 + (AudioCapture.bufferSize * 2 * 1.25 * 2)))];

//        initFileStream(filePath);启动录音时再初始化

        //初始化lame编码器,当前初始化采用lame默认参数,即输入流参数 = 输出流参数
        LameEncode.init(44100, 2, 16, 7);

        //初始化AudioRecord
        audioCapture = new AudioCapture();
        audioCapture.setCaptureListener(this);
    }

    @Override
    public void onCaptureListener(byte[] audioSource, int audioReadSize) {
        //返回byte数组 -- 可自定义调用
    }

    @Override
    public void onCaptureListener(short[] audioSource, int audioReadSize) {
        convertMp3(audioSource, audioReadSize);
        formatPcmData(audioSource);
        calcDecibelValue(audioSource, audioReadSize);
    }

    @Override
    protected void onDestroy() {
        if (audioCapture != null) {
            audioCapture.stop();
        }
        if (fileOutputStream != null) {
            try {
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            fileOutputStream = null;
        }
        super.onDestroy();
    }

    /**
     * pcm转换mp3
     *
     * @param audioSource   :pcm音频数据源
     * @param audioReadSize ：采样数
     */
    private void convertMp3(short[] audioSource, int audioReadSize) {
        //pcm转MP3使用ShortArray
        if (isFinishing() || isDestroyed()) {
            //资源被回收
            return;
        }
        if (audioReadSize <= 0) {
            return;
        }
        if (fileOutputStream == null) {
            return;
        }
        int mp3_byte = LameEncode.encoder(audioSource, mp3_buff, audioReadSize);
        if (mp3_byte < 0) {
            Log.d(TAG, "onCaptureListener: MP3编码失败 :" + mp3_byte);
            return;
        }
        try {
            Log.d(TAG, "onCaptureListener: 编码长度" + mp3_byte);
            fileOutputStream.write(mp3_buff, 0, mp3_byte);
        } catch (IOException e) {
            Log.d(TAG, "onCaptureListener: MP3文件写入失败");
            e.printStackTrace();
        }
    }

    /**
     * 回写lame缓冲区剩余字节数据
     */
    private void writeFlush() {
        int flushResult = LameEncode.flush(mp3_buff);
        if (flushResult > 0) {
            try {
                fileOutputStream.write(mp3_buff, 0, flushResult);
                fileOutputStream.close();
                fileOutputStream = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * FFT格式化pcm数据源
     *
     * @param audioSource ：pcm
     */
    private void formatPcmData(short[] audioSource) {
        formatHandle.post(() -> {
            byte[] data = fftFactory.makeFftData(audioSource);
            byte[] newData = new byte[data.length - 36];
            if (newData.length >= 0) System.arraycopy(data, 36, newData, 0, newData.length);
            dialView.setWaveData(data);
        });
    }

    /**
     * 计算分贝值
     *
     * @param buffer   ：pcm数据源
     * @param readSize ：采样数
     */
    private void calcDecibelValue(short[] buffer, int readSize) {
        if (readSize <= 0) {
            return;
        }
        decibelHandle.postDelayed(() -> {
            long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            for (short i1 : buffer) {
                v += i1 * i1;
            }
            // 平方和除以数据总长度，得到音量大小。
            double mean = v / (double) readSize;
            double volume = 10 * Math.log10(mean);
            Log.d(TAG, "calcDecibelLevel: 分贝值 = " + volume);
            tvDB.setText(String.valueOf((int) volume).concat(" dB"));
        }, 200);
    }

    /**
     * 获取音频文件路径
     *
     * @return String
     */
    private String getFilePath() {
        String currentDate = new SimpleDateFormat("yyyyMMdd_HHmm", Locale.CHINA).format(new Date());
        String fileName = "/manna_".concat(currentDate).concat(".mp3");
        return FileUtils.getDiskCachePath(this) + fileName;
    }

    /**
     * 初始化输出流
     *
     * @param filePath ：文件路径
     */
    private void initFileStream(String filePath) {
        try {
            fileOutputStream = new FileOutputStream(new File(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            fileOutputStream = null;
        }
    }

    /**
     * 初始化当前控件状态
     */
    private void initStatus() {
        tvTitle.setVisibility(View.GONE);
        tvAudioTime.setVisibility(View.VISIBLE);
        //完成
        ivStop.setEnabled(true);
        ivStop.setAlpha(1.0f);
        tvStop.setAlpha(1.0f);

        //查看文件
        ivDetail.setEnabled(false);
        ivDetail.setAlpha(0.4f);
        tvDetail.setAlpha(0.4f);
    }

    /**
     * 重置当前控件状态
     */
    private void resetStatus() {
        recordingTime = 0;
        tvAudioTime.setBase(SystemClock.elapsedRealtime());

        ivStop.setEnabled(false);
        ivStop.setAlpha(0.4f);
        tvStop.setAlpha(0.4f);

        ivDetail.setEnabled(true);
        ivDetail.setAlpha(1.0f);
        tvDetail.setAlpha(1.0f);

        tvStart.setText("开始");
        ivStart.setImageResource(R.mipmap.ic_start_record);

        tvTitle.setVisibility(View.VISIBLE);
        tvAudioTime.setVisibility(View.INVISIBLE);

        tvDB.setText("");
    }

    /**
     * 设置计时器时间
     */
    private void setAudioTime() {
        tvAudioTime.setBase(SystemClock.elapsedRealtime() - recordingTime);
        int hour = (int) ((SystemClock.elapsedRealtime() - tvAudioTime.getBase()) / 1000 / 1000 / 60);
        String timeStr;
        if (hour < 10) {
            timeStr = "0" + hour + ":%s";
        } else {
            timeStr = hour + ":%s";
        }
        tvAudioTime.setFormat(timeStr);
    }
}
