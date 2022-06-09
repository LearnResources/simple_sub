package com.sub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.sub.util.SubDeviceUtil;
import com.sub.util.SubLogUtils;

import java.util.List;

public class SubActivity extends AppCompatActivity implements SubFragmentDelegate {

    private static final String EXTRA_ENABLE_REWARD_ADS = "extra_enable_reward_ads";
    private List<Fragment> orderFragments;
    private boolean enableRewardAds;

    public static void open(Context context, boolean enableRewardAds) {
        SubLogUtils.logD("isEnableRewardAds=" + enableRewardAds);
        Intent intent = new Intent(context, SubActivity.class);
        intent.putExtra(EXTRA_ENABLE_REWARD_ADS, enableRewardAds);
        context.startActivity(intent);
    }

    public static void open(Context context) {
        context.startActivity(new Intent(context, SubActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sm_activity_sub);

        this.enableRewardAds = getIntent().getBooleanExtra(EXTRA_ENABLE_REWARD_ADS, false);

        this.orderFragments = SubScreenManager.getInstance().getConfig().getOrderFragments();
        Log.i("superman", "onCreate: " + orderFragments.size());
        if (!this.showNextFragment()) {
            finish();
        }
    }

    public boolean showNextFragment() {
        try {
            Fragment fragment = orderFragments.get(0);
            showFragment(fragment);
            orderFragments.remove(0);
            return true;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (PurchaseHelper.getInstance().isRemovedAds(this)) {
            return false;
        }
        return false;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.container, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    protected void onDestroy() {
        if (PurchaseHelper.getInstance().isRemovedAds(this)) {
            super.onDestroy();
            return;
        }
        SubRewardAdDelegate rewardAdDelegate = SubScreenManager.getInstance().getConfig().getRewardAdDelegate();
        if (rewardAdDelegate == null) {
            super.onDestroy();
            return;
        }
        if (enableRewardAds && SubDeviceUtil.isConnected(this) && rewardAdDelegate.isAdLoaded()) {
            new Handler().postDelayed(() -> {
                SubRewardActivity.show(this);
            }, 200);
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (!SubScreenManager.getInstance().getConfig().isEnableBack()) {
            return;
        }

        if (this.showNextFragment()) {
            return;
        }
        finish();
    }
}
