package com.sub.example.sub;

import androidx.annotation.NonNull;
import androidx.annotation.Size;

public interface SubAppDelegate {
    void registerNotificationTopic(String topic);

    void unregisterNotificationTopic(String topic);

    void logEvent(@NonNull @Size(min = 1L, max = 32L) String event);
}
