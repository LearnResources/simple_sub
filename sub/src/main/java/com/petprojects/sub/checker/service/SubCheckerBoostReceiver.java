package com.petprojects.sub.checker.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.petprojects.sub.checker.SubCheckerScheduler;

public class SubCheckerBoostReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }

        if (Intent.ACTION_BOOT_COMPLETED.equalsIgnoreCase(action)
                || Intent.ACTION_DATE_CHANGED.equalsIgnoreCase(action)
                || Intent.ACTION_USER_PRESENT.equalsIgnoreCase(action)) {
            SubCheckerScheduler.scheduler(context);
        }
    }
}
