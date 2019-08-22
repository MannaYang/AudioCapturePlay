#include "lame_3.99.5_libmp3lame/lame.h"
#include "com_czt_mp3recorder_util_LameUtil.h"
#include <stdio.h>
#include <stdlib.h>
#include <jni.h>
#include <sys/stat.h>

static lame_global_flags *lame = NULL;


JNIEXPORT void JNICALL Java_com_czt_mp3recorder_util_LameUtil_init(
		JNIEnv *env, jclass cls, jint inSamplerate, jint inChannel, jint outSamplerate, jint outBitrate, jint quality) {
	if (lame != NULL) {
		lame_close(lame);
		lame = NULL;
	}
	lame = lame_init();
	lame_set_in_samplerate(lame, inSamplerate);
	lame_set_num_channels(lame, inChannel);//输入流的声道
	lame_set_out_samplerate(lame, outSamplerate);
	lame_set_brate(lame, outBitrate);
	lame_set_quality(lame, quality);
	lame_init_params(lame);
}

JNIEXPORT jint JNICALL Java_com_czt_mp3recorder_util_LameUtil_encode(
		JNIEnv *env, jclass cls, jshortArray buffer_l, jshortArray buffer_r,
		jint samples, jbyteArray mp3buf) {
	jshort* j_buffer_l = (*env)->GetShortArrayElements(env, buffer_l, NULL);

	jshort* j_buffer_r = (*env)->GetShortArrayElements(env, buffer_r, NULL);

	const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);
	jbyte* j_mp3buf = (*env)->GetByteArrayElements(env, mp3buf, NULL);

	int result = lame_encode_buffer(lame, j_buffer_l, j_buffer_r,
			samples, j_mp3buf, mp3buf_size);

	(*env)->ReleaseShortArrayElements(env, buffer_l, j_buffer_l, 0);
	(*env)->ReleaseShortArrayElements(env, buffer_r, j_buffer_r, 0);
	(*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buf, 0);

	return result;
}

JNIEXPORT jint JNICALL Java_com_czt_mp3recorder_util_LameUtil_flush(
		JNIEnv *env, jclass cls, jbyteArray mp3buf) {
	const jsize mp3buf_size = (*env)->GetArrayLength(env, mp3buf);
	jbyte* j_mp3buf = (*env)->GetByteArrayElements(env, mp3buf, NULL);

	int result = lame_encode_flush(lame, j_mp3buf, mp3buf_size);

	(*env)->ReleaseByteArrayElements(env, mp3buf, j_mp3buf, 0);

	return result;
}

JNIEXPORT void JNICALL Java_com_czt_mp3recorder_util_LameUtil_close
(JNIEnv *env, jclass cls) {
	lame_close(lame);
	lame = NULL;
}


/**
 * 返回值 char* 这个代表char数组的首地址
 *  Jstring2CStr 把java中的jstring的类型转化成一个c语言中的char 字符串
 */
char* Jstring2CStr(JNIEnv* env, jstring jstr) {
	char* rtn = NULL;
	jclass clsstring = (*env)->FindClass(env, "java/lang/String"); //String
	jstring strencode = (*env)->NewStringUTF(env, "GB2312"); // 得到一个java字符串 "GB2312"
	jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes",
			"(Ljava/lang/String;)[B"); //[ String.getBytes("gb2312");
	jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid,
			strencode); // String .getByte("GB2312");
	jsize alen = (*env)->GetArrayLength(env, barr); // byte数组的长度
	jbyte* ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
	if (alen > 0) {
		rtn = (char*) malloc(alen + 1); //"\0"
		memcpy(rtn, ba, alen);
		rtn[alen] = 0;
	}
	(*env)->ReleaseByteArrayElements(env, barr, ba, 0); //
	return rtn;
}

int flag = 0;
/**
 * wav转换mp3
 */
JNIEXPORT void JNICALL Java_com_czt_mp3recorder_util_LameUtil_convert
(JNIEnv * env, jobject obj, jstring jwav, jstring jmp3, jint inSamplerate) {

	//1.初始化lame的编码器
	lame_t lameConvert =  lame_init();
	int channel = 1;//单声道

	//2. 设置lame mp3编码的采样率
	lame_set_in_samplerate(lameConvert , inSamplerate);
	lame_set_out_samplerate(lameConvert, inSamplerate);
	lame_set_num_channels(lameConvert,1);

	// 3. 设置MP3的编码方式
	lame_set_VBR(lameConvert, vbr_default);
	lame_init_params(lameConvert);

	char* cwav =Jstring2CStr(env,jwav) ;
	char* cmp3=Jstring2CStr(env,jmp3);

	const int SIZE = (inSamplerate/20)+7200;

	//4.打开 wav,MP3文件
	FILE* fwav = fopen(cwav,"rb");
	fseek(fwav, 4*1024, SEEK_CUR);
	FILE* fmp3 = fopen(cmp3,"wb+");


	short int wav_buffer[SIZE*channel];
	unsigned char mp3_buffer[SIZE];


	int read ; int write; //代表读了多少个次 和写了多少次
	int total=0; // 当前读的wav文件的byte数目
	do{
		if(flag==404){
			return;
		}
		read = fread(wav_buffer,sizeof(short int)*channel, SIZE,fwav);
		total +=  read* sizeof(short int)*channel;
		if(read!=0){
			write = lame_encode_buffer(lameConvert, wav_buffer, NULL, read, mp3_buffer, SIZE);
			//write = lame_encode_buffer_interleaved(lame,wav_buffer,read,mp3_buffer,SIZE);
		}else{
		write = lame_encode_flush(lameConvert,mp3_buffer,SIZE);
		}
		//把转化后的mp3数据写到文件里
        fwrite(mp3_buffer,sizeof(unsigned char),write,fmp3);

	}while(read!=0);
    lame_mp3_tags_fid(lameConvert,fmp3);
	lame_close(lameConvert);
	fclose(fwav);
	fclose(fmp3);
}