package com.manna.capture.capture;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.manna.capture.R;
import com.manna.capture.adapter.BaseAdapter;
import com.manna.capture.adapter.ItemViewType;
import com.manna.capture.utils.AudioFileUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 录音文件列表、解码播放
 */
public class AudioFileActivity extends AppCompatActivity {

    private String dirFilePath;
    private RecyclerView rvAudioFile;
    private BaseAdapter baseAdapter;
    private AudioFileItemType itemType;
    private List<ItemViewType> typeList;
    private List<AudioFileEntity> list;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audio_file);

        initView();
        initData();
    }

    private void initView() {
        rvAudioFile = findViewById(R.id.rv_audio_file);
        dirFilePath = getIntent().getStringExtra("dirFilePath");
    }

    //初始化音频列表
    private void initData() {
        list = AudioFileUtils.getAllFiles(this,dirFilePath, "mp3");
        itemType = new AudioFileItemType(list, (AudioFileItemType.AudioPlayListener) this::playMusic);

        typeList = new ArrayList<>();
        typeList.add(itemType);
        baseAdapter = new BaseAdapter(this, typeList);

        rvAudioFile.setLayoutManager(new LinearLayoutManager(this));
        rvAudioFile.setAdapter(baseAdapter);
    }

    /**
     * 播放音乐
     * @param filePath 文件路径
     * @param fileName 文件名称
     */
    private void playMusic(String filePath, String fileName) {
        startActivity(new Intent(this, AudioPlayActivity.class)
                .putExtra("filePath", filePath).putExtra("fileName", fileName));
    }
}
