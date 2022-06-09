package com.petprojects.sub;

import android.app.Activity;
import android.content.Context;

public interface SubRewardAdDelegate {
    void loadAd(Context context);

    void show(Activity context);

    boolean isAdLoaded();
}
