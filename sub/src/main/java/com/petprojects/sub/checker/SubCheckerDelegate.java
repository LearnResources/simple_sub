package com.petprojects.sub.checker;

import android.content.Context;

import com.petprojects.sub.checker.model.MessageItem;

public interface SubCheckerDelegate {
    void pushNotification(Context context, MessageItem message);
}
