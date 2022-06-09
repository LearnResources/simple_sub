package com.sub.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

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

    public static int dimenToPixel(Context context, int dimenResourceId) {
        return context.getResources().getDimensionPixelSize(dimenResourceId);
    }

}
