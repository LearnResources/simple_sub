package com.petprojects.sub.util;

import android.content.Context;

public class SubPref extends BasePrefData {
    private static final String K_LOCAL_PURCHASED = "local_purchased";
    private static final String K_LOCAL_SUBSCRIBED = "local_subscribed";

    private static SubPref instance;

    protected SubPref(Context context) {
        super(context, "sub");
    }

    public static void init(Context context) {
        instance = new SubPref(context);
    }

    public static SubPref get() {
        return instance;
    }

    public void updateLocalPurchasedState(boolean purchased) {
        putBoolean(K_LOCAL_PURCHASED, purchased);
    }

    public boolean isLocalPurchasedState() {
        return getBoolean(K_LOCAL_PURCHASED, false);
    }

    public boolean isLocalSubscribedState() {
        return getBoolean(K_LOCAL_SUBSCRIBED, false);
    }

    public void updateLocalSubscribedState(boolean purchased) {
        putBoolean(K_LOCAL_SUBSCRIBED, purchased);
    }

}
