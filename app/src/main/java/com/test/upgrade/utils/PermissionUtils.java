package com.test.upgrade.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by dell on 2017/11/28.
 */

public class PermissionUtils {
    public static final String READ_EXTERNAL= Manifest.permission.READ_EXTERNAL_STORAGE;
    public static final int REQUEST_READ_EXTERNA=1;

    public static void checkPermission(Context context,String[] permissions,String permission,int requestCode){
        if(ContextCompat.checkSelfPermission(context,permission)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions((Activity) context,permissions,REQUEST_READ_EXTERNA);
        }
    }
}
