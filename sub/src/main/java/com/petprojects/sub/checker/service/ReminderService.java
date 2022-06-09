package com.petprojects.sub.checker.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.JobIntentService;

import com.petprojects.sub.checker.SubAppChecker;
import com.petprojects.sub.checker.SubCheckerAction;

public class ReminderService extends JobIntentService {
    private static final int JOB_ID = 135329;

    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, ReminderService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        try {
            String action = intent.getAction();
            Log.i("superman", "onHandleWork: " + action);
            if (SubCheckerAction.ACTION_SUB_CHECKER.equalsIgnoreCase(action)) {
                requestShowNotification();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void requestShowNotification() {
        SubAppChecker subAppChecker = SubAppChecker.instance();
        subAppChecker.pushRandomNotification(this);
    }
    

}