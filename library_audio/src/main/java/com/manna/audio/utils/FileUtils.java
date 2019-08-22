package com.manna.audio.utils;

import android.content.Context;
import android.os.Environment;
import android.text.format.Formatter;

public class FileUtils {
    /**
     * 读取缓存目录
     *
     * @param context :context
     * @return String
     */
    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() == null) {
                return "";
            }
            return context.getExternalCacheDir().getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }

    /**
     * 计算文件大小
     *
     * @param length
     * @return
     */
    public static String ShowLongFileSize(Long length) {
        if (length >= 1048576) {
            return (length / 1048576) + "MB";
        } else if (length >= 1024) {
            return (length / 1024) + "KB";
        } else if (length < 1024) {
            return length + "B";
        } else {
            return "0KB";
        }
    }
}
