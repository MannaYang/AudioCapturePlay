
package com.manna.capture.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

/**
 * 权限申请
 */
public class PermissionUtils {
    private static final int REQUEST_CODE = 1000;

    public static boolean hasAudioPermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasWriteStoragePermission(Activity activity) {
        return ContextCompat.checkSelfPermission(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestAudioPermission(Activity activity, boolean requestWritePermission) {

        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.RECORD_AUDIO) || (requestWritePermission &&
                ActivityCompat.shouldShowRequestPermissionRationale(activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE));
        if (showRationale) {
            Toast.makeText(activity, "应用缺少录音权限", Toast.LENGTH_LONG).show();
        } else {

            String permissions[] = requestWritePermission ? new String[]{Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE} : new String[]{Manifest.permission.RECORD_AUDIO};
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
        }
    }

    public static void requestWriteStoragePermission(Activity activity) {
        boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (showRationale) {
            Toast.makeText(activity, "应用缺少读写文件权限", Toast.LENGTH_LONG).show();
        } else {
            String permissions[] = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE);
        }
    }

    /**
     * 设置权限
     */
    public static void launchPermissionSettings(Activity activity) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        activity.startActivity(intent);
    }
}
