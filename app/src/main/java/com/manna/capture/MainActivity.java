package com.manna.capture;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.manna.capture.R;
import com.manna.capture.capture.AudioCaptureActivity;
import com.manna.capture.utils.PermissionUtils;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private TextView tvAudioRecord, tvVideoRecord;
    //录视频项目地址
    private static final String MSG = "开源项目地址为 : https://github.com/MannaYang/AudioVideoCodec";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }

    public void initView() {
        tvAudioRecord = findViewById(R.id.tv_audio_record);
        tvVideoRecord = findViewById(R.id.tv_video_record);

        tvAudioRecord.setOnClickListener(v -> {

            if (PermissionUtils.hasAudioPermission(this)) {
                openAudioCapture();
            } else {
                //获取缓存目录
                PermissionUtils.requestAudioPermission(this, false);
            }
        });

        tvVideoRecord.setOnClickListener(v -> {
            //录视频参看我另外的开源项目,地址为：https://github.com/MannaYang/AudioVideoCodec
            Toast.makeText(this, MSG, Toast.LENGTH_LONG).show();
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (!PermissionUtils.hasAudioPermission(this)) {
            Toast.makeText(this,
                    "应用缺少录音权限", Toast.LENGTH_LONG).show();
            PermissionUtils.launchPermissionSettings(this);
        } else {
            openAudioCapture();
        }
    }

    private void openAudioCapture() {
        startActivity(new Intent(this, AudioCaptureActivity.class));
    }
}
