package com.manna.capture.capture;

/**
 * 录音文件信息
 */
public class AudioFileEntity {
    //文件名称
    private String fileName;
    //创建时间
    private String createTime;
    //录音时长
    private String audioTime;
    //文件路径
    private String filePath;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getAudioTime() {
        return audioTime;
    }

    public void setAudioTime(String audioTime) {
        this.audioTime = audioTime;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
