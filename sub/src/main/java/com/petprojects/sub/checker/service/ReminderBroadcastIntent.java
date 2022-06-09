package com.petprojects.sub.checker.service;

import android.content.Context;
import android.content.Intent;

import com.petprojects.sub.checker.SubCheckerAction;

public class ReminderBroadcastIntent {

    public static Intent getQuotesRequestIntent(Context context) {
        Intent intent = new Intent(context, ReminderReceiver.class);
        intent.setAction(SubCheckerAction.ACTION_SUB_CHECKER);
        return intent;
    }
}
