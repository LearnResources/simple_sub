package com.sub.example.sub.event;

import org.greenrobot.eventbus.EventBus;

public class PurchasedEvent {
    public static void sendEvent() {
        EventBus.getDefault().post(new PurchasedEvent());
    }
}
