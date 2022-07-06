package com.sub.example.sub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.petprojects.sub.PurchaseHelper;
import com.petprojects.sub.SubBillingPriceCallback;
import com.sub.example.R;

import java.util.ArrayList;
import java.util.List;

public class SubActivity extends AppCompatActivity implements SubFragmentDelegate {

    private static final String EXTRA_ENABLE_REWARD_ADS = "extra_enable_reward_ads";
    private static final String EXTRA_SUB_STYLE = "extra_sub_style";
    private static final String EXTRA_COMEBACK_MAIN = "extra_comeback_main";

    private List<Fragment> orderFragments;
    private boolean enableRewardAds;
    private boolean comebackMain;
    private boolean viewLoaded;

    public static void open(Context context, boolean enableRewardAds) {
        Intent intent = new Intent(context, SubActivity.class);
        intent.putExtra(EXTRA_ENABLE_REWARD_ADS, enableRewardAds);
        context.startActivity(intent);
    }

    public static void open(Context context, String style, boolean comebackMain) {
        Intent intent = new Intent(context, SubActivity.class);
        intent.putExtra(EXTRA_SUB_STYLE, style);
        intent.putExtra(EXTRA_COMEBACK_MAIN, comebackMain);
        context.startActivity(intent);
    }

    public static void open(Context context) {
        context.startActivity(new Intent(context, SubActivity.class));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sm_activity_sub);

        PurchaseHelper.getInstance().loadBillingPriceAsync(this, new SubBillingPriceCallback() {
            @Override
            public void onSuccess() {
                extracted();
            }

            @Override
            public void onFailure() {
            }
        });

        extracted();
    }

    private void extracted() {
        if (this.viewLoaded) {
            return;
        }
        this.viewLoaded = true;
        Intent intent = getIntent();
        this.enableRewardAds = intent.getBooleanExtra(EXTRA_ENABLE_REWARD_ADS, false);
        this.comebackMain = intent.getBooleanExtra(EXTRA_COMEBACK_MAIN, false);
        String subStyle = intent.getStringExtra(EXTRA_SUB_STYLE);
        Fragment subFragment = SubScreenManager.getInstance().getConfig().getSubFragment(subStyle);
        try {
            if (subFragment == null) {
                this.orderFragments = SubScreenManager.getInstance().getConfig().getOrderFragments();
            } else {
                List<Fragment> objects = new ArrayList<>();
                objects.add(subFragment);
                this.orderFragments = objects;
            }
            if (!this.showNextFragment()) {
                finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
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

//    @Override
//    protected void onDestroy() {
//        if (PurchaseHelper.getInstance().isSubscribed(this)) {
//            super.onDestroy();
//            return;
//        }
//        SubRewardAdDelegate rewardAdDelegate = SubScreenManager.getInstance().getConfig().getRewardAdDelegate();
//        if (rewardAdDelegate == null) {
//            super.onDestroy();
//            return;
//        }
//        if (enableRewardAds && SubDeviceUtil.isConnected(this) && rewardAdDelegate.isAdLoaded()) {
//            new Handler().postDelayed(() -> {
//                SubRewardActivity.show(this);
//            }, 200);
//        }
//        super.onDestroy();
//    }

    @Override
    public void finish() {
        try {
            if (comebackMain) {
                super.finish();
                Log.i("superman", "finish: " + comebackMain);
                SubScreenManager.getInstance().getConfig().getSubAppDelegate().openMain(this);
            }else{
                super.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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
