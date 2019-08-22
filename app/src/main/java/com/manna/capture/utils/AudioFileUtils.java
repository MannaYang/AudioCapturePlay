package com.manna.capture.utils;

import android.content.Context;
import android.text.format.Formatter;

import com.manna.capture.capture.AudioFileEntity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AudioFileUtils {
    /**
     * 获取指定目录内所有文件路径
     *
     * @param dirPath  文件目录
     * @param fileType 文件后缀类型
     */
    public static List<AudioFileEntity> getAllFiles(Context context, String dirPath, String fileType) {
        File f = new File(dirPath);
        if (!f.exists()) {//判断路径是否存在
            return null;
        }
        List<AudioFileEntity> list = new ArrayList<>();
        File[] files = f.listFiles();

        if (files == null) {//判断权限
            return null;
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.CHINA);
        for (File file : files) {//遍历目录
            if (file.isFile() && file.getName().endsWith(fileType)) {
                AudioFileEntity entity = new AudioFileEntity();
                int end = file.getName().lastIndexOf('.');
                //获取文件名
                String fileName = file.getName().substring(0, end);
                //获取文件路径
                String filePath = file.getAbsolutePath();
                //创建时间
                String createTime = dateFormat.format(file.lastModified());
                //文件大小
                String fileSize = formatSize(context, file.length());

                entity.setFileName(fileName);
                entity.setFilePath(filePath);
                entity.setCreateTime(createTime);
                entity.setAudioTime(fileSize);
                list.add(entity);
            } else if (file.isDirectory()) {//查询子目录
                getAllFiles(context, file.getAbsolutePath(), fileType);
            } else {
            }
        }
        return list;
    }

    /**
     * 格式化数据
     *
     * @param context  ：context
     * @param fileSize ：byte
     * @return String
     */
    public static String formatSize(Context context, long fileSize) {
        return Formatter.formatFileSize(context, fileSize);
    }
}
