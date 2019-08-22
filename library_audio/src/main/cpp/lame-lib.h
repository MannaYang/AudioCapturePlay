//
// Created by 15971 on 2019/7/18.
//


#ifndef AUDIO_LAME_LIB_H
#define AUDIO_LAME_LIB_H

#include "../../../../../../AndroidSDK/ndk-bundle/toolchains/llvm/prebuilt/windows-x86_64/sysroot/usr/include/jni.h"

#endif //AUDIO_LAME_LIB_H
extern "C" void wavConvertToMP3(JNIEnv *env, jobject clazz, jstring wavFilePath,
                                jstring mp3FilePath, jint sampleRate,
                                jint channels, jint outBitrate);

extern "C" void
pcmToMP3(JNIEnv *env, jobject clazz, jstring mp3FilePath, jint sampleRate, jint channelCount,
        jshortArray pcmArray,jint readByte);