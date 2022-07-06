package com.sub.example.sub.checker;

import android.content.Context;

import com.sub.example.sub.checker.model.MessageItem;

public interface SubCheckerDelegate {
    void pushNotification(Context context, MessageItem message);
}
