package com.sub.example.sub.checker.model;

import android.content.Context;

import androidx.annotation.StringRes;

import com.google.gson.annotations.SerializedName;

public class MessageItem {

    @SerializedName("title")
    private String title;

    @SerializedName("desc")
    private String desc;

    public MessageItem(Context context, int title, int desc) {
        this.title = context.getString(title);
        this.desc = context.getString(desc);
    }

    public String getTitle() {
        return title;
    }

    public String getDesc() {
        return desc;
    }

    public static MessageItem create(Context context, @StringRes int title, @StringRes int desc) {
        return new MessageItem(context, title, desc);
    }
}