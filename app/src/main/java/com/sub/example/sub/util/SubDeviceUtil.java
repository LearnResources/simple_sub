package com.sub.example.sub.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;

public class SubDeviceUtil {
    public static NetworkInfo getNetworkInfo(Context context) {
        if (context == null)
            return null;
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return null;
        }
        return cm.getActiveNetworkInfo();
    }

    public static boolean isConnected(Context context) {
        NetworkInfo info = getNetworkInfo(context);
        return (info != null && info.isConnected());
    }

    public static long getInstalledTime(Context context) {
        PackageManager pm = context.getPackageManager();
        ApplicationInfo appInfo = null;
        try {
            appInfo = pm.getApplicationInfo(context.getPackageName(), 0);
            String appFile = appInfo.sourceDir;
            return new File(appFile).lastModified();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();

        }
        return 0L;
    }

}
