package com.petprojects.sub.checker.service;

import android.app.PendingIntent;
import android.content.Context;

import java.util.Calendar;

public class ReminderHelper {

    public static void schedule(Context context, Calendar calendar) {
        PendingIntent pendingIntent = getReminderIntent(context, calendar);
        ReminderReceiver.schedule(context, pendingIntent, calendar);
    }

    private static PendingIntent getReminderIntent(Context context, Calendar calendar) {
        return PendingIntent.getBroadcast(context, (int) calendar.getTimeInMillis(), ReminderBroadcastIntent.getQuotesRequestIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void cancel(Context context, Calendar calendar) {
        PendingIntent pendingIntent = getReminderIntent(context, calendar);
        ReminderReceiver.cancel(context, pendingIntent);
    }

}
