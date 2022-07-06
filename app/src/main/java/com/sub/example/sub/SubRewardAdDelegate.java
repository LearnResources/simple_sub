package com.sub.example.sub;

import android.app.Activity;
import android.content.Context;

public interface SubRewardAdDelegate {
    void loadAd(Context context);

    void show(Activity context);

    boolean isAdLoaded();
}
