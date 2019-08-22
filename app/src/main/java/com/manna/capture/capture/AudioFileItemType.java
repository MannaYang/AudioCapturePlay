package com.manna.capture.capture;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.manna.capture.R;
import com.manna.capture.adapter.ItemViewType;

import java.util.List;

/**
 * RecyclerView - holder
 */
public class AudioFileItemType implements ItemViewType {

    private List<AudioFileEntity> list;
    private AudioPlayListener listener;

    public AudioFileItemType(List<AudioFileEntity> list, AudioPlayListener listener) {
        this.list = list;
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_audio_file;
    }

    @Override
    public RecyclerView.ViewHolder getViewHolder(View viewHolder) {
        return new ItemViewHolder(viewHolder);
    }

    @Override
    public void bindViewHolder(RecyclerView.ViewHolder viewHolder, int index) {
        ItemViewHolder holder = (ItemViewHolder) viewHolder;
        AudioFileEntity entity = list.get(index);
        holder.fileName.setText(entity.getFileName());
        holder.createTime.setText(entity.getCreateTime());
        holder.audioTime.setText(entity.getAudioTime());
        holder.ivPlay.setOnClickListener(v -> listener.audioPlay(entity.getFilePath(),entity.getFileName()));
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {

        private TextView fileName, createTime, audioTime;
        private ImageView ivPlay;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            fileName = itemView.findViewById(R.id.tv_file_name);
            createTime = itemView.findViewById(R.id.tv_create_time);
            audioTime = itemView.findViewById(R.id.tv_audio_time);
            ivPlay = itemView.findViewById(R.id.iv_play);
        }
    }

    //播放录音
    public interface AudioPlayListener {
        void audioPlay(String filePath, String fileName);
    }
}
