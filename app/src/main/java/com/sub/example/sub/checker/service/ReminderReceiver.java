package com.sub.example.sub.checker.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.format.DateFormat;
import android.util.Log;

import com.sub.example.sub.checker.SubAppChecker;
import com.sub.example.sub.checker.SubCheckerAction;
import com.sub.example.sub.checker.SubCheckerScheduler;
import com.petprojects.sub.util.SubLogUtils;

import java.util.Calendar;

public class ReminderReceiver extends BroadcastReceiver {

    public static void schedule(Context context, PendingIntent alarmIntent, Calendar calendar) {
        try {
            AlarmManager alarmMgr = getAlarmService(context);
            Log.i("superman", DateFormat.format("schedule:  dd-MM-yyyy HH:mm:ss", calendar).toString());
            SubLogUtils.logI(DateFormat.format("schedule:  dd-MM-yyyy HH:mm:ss", calendar).toString());

            int SDK_INT = Build.VERSION.SDK_INT;
            if (SDK_INT < Build.VERSION_CODES.KITKAT)
                alarmMgr.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
            else if (SDK_INT < Build.VERSION_CODES.M)
                alarmMgr.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
            else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmMgr.canScheduleExactAlarms()) {
                    return;
                }
                alarmMgr.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), alarmIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static AlarmManager getAlarmService(Context context) {
        return (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (isInitialStickyBroadcast()) {
            return;
        }

        String action = intent.getAction();
        Log.i("superman", "onReceive: " + action);

        try {
            if (SubCheckerAction.ACTION_SUB_CHECKER.equalsIgnoreCase(action)) {
                try {
                    SubAppChecker.instance().getAppDelegate().logEvent("SUB_NOTIFICATION_CH_SALE");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ReminderService.enqueueWork(context, intent);
                SubCheckerScheduler.scheduler(context);
            }

        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void cancel(Context context, PendingIntent pendingIntent) {
        getAlarmService(context).cancel(pendingIntent);
    }
}