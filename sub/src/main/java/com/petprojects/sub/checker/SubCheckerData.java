package com.petprojects.sub.checker;

import android.content.Context;

import com.petprojects.sub.base.BasePrefData;

import java.lang.ref.WeakReference;

public class SubCheckerData extends BasePrefData {
    private static final String PREF_NAME = "sub_checker";
    private static final String K_OPEN_APP = "open_app";
    private static WeakReference<SubCheckerData> instance;

    protected SubCheckerData(Context context) {
        super(context, PREF_NAME);
    }

    public static SubCheckerData instance(Context context) {
        if (instance == null || instance.get() == null) {
            instance = new WeakReference<>(new SubCheckerData(context));
        }
        return instance.get();
    }

    public int countOpen() {
        int oldValue = getInt(K_OPEN_APP, 0);
        oldValue++;
        putInt(K_OPEN_APP, oldValue);
        return oldValue;
    }

    public void clearOpenLog() {
        remove(K_OPEN_APP);
    }

    public int getNumberOfOpenApp() {
        return getInt(K_OPEN_APP, 0);
    }
}
