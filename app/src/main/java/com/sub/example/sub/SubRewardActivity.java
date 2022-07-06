package com.sub.example.sub;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.sub.example.R;
import com.sub.example.base.BaseSubActivity;

public class SubRewardActivity extends BaseSubActivity {
    public static void show(Context context) {
        context.startActivity(new Intent(context, SubRewardActivity.class));
    }

    @Override
    protected void initViews(Bundle savedInstanceState) {
        Activity mainActivity = SubScreenManager.getMainActivity();
        if (mainActivity != null && mainActivity.isFinishing()) {
            SubScreenManager.destroyActivityInstance();
            finish();
            return;
        }
        findViewById(R.id.bt_watch_now).setOnClickListener(v -> {
            SubScreenManager.getInstance().getConfig().getRewardAdDelegate().show(this);
            finish();
        });
        findViewById(R.id.bt_cancel).setOnClickListener(v -> finish());
    }

    @Override
    public int onLayout() {
        return R.layout.sm_activity_reward;
    }

    @Override
    public void onBackPressed() {
    }
}
