package com.manna.audio;

/**
 * 调用lame-lib.so中的native方法
 */
public class LameEncode {
    //加载lame-lib.so
    static {
        System.loadLibrary("lame-lib");
    }

    /**
     * 初始化lame,cpp中初始化采用lame默认参数配置
     *
     * @param sampleRate     ：采样率 -- 录音默认44100
     * @param channelCount   ：通道数 -- 录音默认双通道2
     * @param audioFormatBit ：位宽 -- 录音默认ENCODING_PCM_16BIT 16bit
     * @param quality        ：MP3音频质量 0~9 其中0是最好，非常慢，9是最差  2=high(高)  5 = medium(中)  7=low(低)
     */
    public static native void init(int sampleRate, int channelCount, int audioFormatBit, int quality);

    /**
     * 启用lame编码
     *
     * @param pcmBuffer  ：音频数据源
     * @param mp3_buffer ：写入MP3数据buffer
     * @param sample_num ：采样个数
     */
    public static native int encoder(short[] pcmBuffer, byte[] mp3_buffer, int sample_num);

    /**
     * 刷新缓冲器
     *
     * @param mp3_buffer ：MP3编码buffer
     * @return int 返回剩余编码器字节数据,需要写入文件
     */
    public static native int flush(byte[] mp3_buffer);

    /**
     * 释放编码器
     */
    public static native void close();
}
