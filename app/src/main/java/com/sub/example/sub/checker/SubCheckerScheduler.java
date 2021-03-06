package com.sub.example.sub.checker;

import android.content.Context;
import android.util.Log;

import com.sub.example.sub.SubScreenManager;
import com.sub.example.sub.checker.service.ReminderHelper;
import com.sub.example.sub.util.SubDeviceUtil;
import com.sub.example.sub.util.SubConfigPrefs;

import java.util.Calendar;
import java.util.List;

public class SubCheckerScheduler {

    public static void scheduler(Context context) {
        SubConfigPrefs.init(context);
        boolean enableSubScript = SubConfigPrefs.get().isEnableSubScript();
        if (!enableSubScript) {
            SubAppChecker.instance().getAppDelegate().unregisterNotificationTopic(SubCheckerTopic.SUB_CANCEL);
        }
        List<Integer> subDays = SubConfigPrefs.get().getSubDays();
        long installedTime = SubDeviceUtil.getInstalledTime(context);
        for (Integer subDay : subDays) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(installedTime);
            calendar.add(SubScreenManager.getInstance().isDebugMode() ? Calendar.MINUTE : Calendar.DATE, subDay);
            if (!enableSubScript) {
                ReminderHelper.cancel(context, calendar);
                continue;
            }
            Log.i("superman", "scheduler: " + calendar.getTimeInMillis());
            if (calendar.getTimeInMillis() > System.currentTimeMillis()) {
                ReminderHelper.schedule(context, calendar);
            } else {
                Log.i("superman", "scheduler: no no");
            }
        }
        Log.i("superman", "scheduler: " + subDays + "   " + installedTime);
    }
}
