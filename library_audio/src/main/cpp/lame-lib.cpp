
#include <stdio.h>
#include <cstdlib>
#include <jni.h>
#include <sys/stat.h>
#include <iosfwd>
#include <fstream>
#include "libmp3lame/lame.h"

//lame 对象
static lame_global_flags *gfp = NULL;

//jstring转string -- defined in lame_util.c 69 lines
char *Jstring2CStr(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = env->FindClass("java/lang/String"); //String
    jstring strencode = env->NewStringUTF("GB2312"); // 得到一个java字符串 "GB2312"
    jmethodID mid = env->GetMethodID(clsstring, "getBytes",
                                     "(Ljava/lang/String;)[B"); //[ String.getBytes("gb2312");
    jbyteArray barr = (jbyteArray) env->CallObjectMethod(jstr, mid,
                                                         strencode); // String .getByte("GB2312");
    jsize alen = env->GetArrayLength(barr); // byte数组的长度
    jbyte *ba = env->GetByteArrayElements(barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1); //"\0"
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    env->ReleaseByteArrayElements(barr, ba, 0); //
    return rtn;
}

//初始化lame参数
extern "C" JNIEXPORT void JNICALL
Java_com_manna_audio_LameEncode_init(JNIEnv *env, jclass jclass1, jint sampleRate,
                                     jint channelCount,
                                     jint audioFormatBit, jint quality) {
    //初始化lame -- 采用默认输入音频参数配置，转换为MP3后，bitrate比特率为128kbps
    gfp = lame_init();
    //采样率
//    lame_set_in_samplerate(gfp, sampleRate);
    //声道数
//    lame_set_num_channels(gfp, channelCount);
    //输入采样率
//    lame_set_out_samplerate(gfp, sampleRate);
    //位宽
//    lame_set_brate(gfp, audioFormatBit);
    //音频质量
//    lame_set_quality(gfp, quality);
    //初始化参数配置
    lame_init_params(gfp);
}

//开启MP3编码
extern "C" JNIEXPORT jint JNICALL
Java_com_manna_audio_LameEncode_encoder(JNIEnv *env, jclass jclass1, jshortArray pcm_buffer,
                                        jbyteArray mp3_buffer, jint sample_num) {
    //lame转换需要short指针参数
    jshort *pcm_buf = env->GetShortArrayElements(pcm_buffer, JNI_FALSE);
    //获取MP3数组长度
    const jsize mp3_buff_len = env->GetArrayLength(mp3_buffer);
    //获取buffer指针
    jbyte *mp3_buf = env->GetByteArrayElements(mp3_buffer, JNI_FALSE);
    //编译后得bytes
    int encode_result;
    //根据输入音频声道数判断
    if (lame_get_num_channels(gfp) == 2) {
        encode_result = lame_encode_buffer_interleaved(gfp, pcm_buf, sample_num / 2, mp3_buf,
                                                       mp3_buff_len);
    } else {
        encode_result = lame_encode_buffer(gfp, pcm_buf, pcm_buf, sample_num, mp3_buf,
                                           mp3_buff_len);
    }
    //释放资源
    env->ReleaseShortArrayElements(pcm_buffer, pcm_buf, 0);
    env->ReleaseByteArrayElements(mp3_buffer, mp3_buf, 0);
    return encode_result;
}

//关闭MP3编码buffer
extern "C" JNIEXPORT jint JNICALL
Java_com_manna_audio_LameEncode_flush(JNIEnv *env, jclass jclass1, jbyteArray mp3_buffer) {
    //获取MP3数组长度
    const jsize mp3_buff_len = env->GetArrayLength(mp3_buffer);
    //获取buffer指针
    jbyte *mp3_buf = env->GetByteArrayElements(mp3_buffer, JNI_FALSE);
    //刷新编码器缓冲，获取残留在编码器缓冲里的数据
    int flush_result = lame_encode_flush(gfp, mp3_buf, mp3_buff_len);
    env->ReleaseByteArrayElements(mp3_buffer, mp3_buf, 0);
    return flush_result;
}

//释放编码器
JNIEXPORT void JNICALL
Java_com_manna_audio_LameEncode_close(JNIEnv *env, jclass type) {
    lame_close(gfp);
}