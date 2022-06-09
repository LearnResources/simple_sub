package com.petprojects.sub;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.petprojects.sub.util.SubDeviceUtil;
import com.petprojects.sub.util.SubLogUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class SubScreenManager {
    private static SubScreenManager instance;
    private final SubScreenConfig config;
    private static Activity mainActivity;
    private boolean debugMode;

    SubScreenManager(SubScreenConfig config, boolean debugMode) {
        this.config = config;
        this.debugMode = debugMode;
        Context context = config.getContext();
        SubConfigPrefs.init(context);
        PurchaseHelper.getInstance().initBilling(context);
    }

    public SubScreenManager setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
        return this;
    }

    public static void init(SubScreenConfig config) {
        instance = new SubScreenManager(config, false);
    }

    public static boolean openAndComebackMain(Context context, String style) {
        return openAndComebackMain(context, style, true);
    }

    public static boolean openAndComebackMain(Context context, String style, boolean comeback) {
        if (PurchaseHelper.getInstance().isRemovedAds(context)) {
            return false;
        }
        SubActivity.open(context, style, comeback);
        return true;
    }

    public static void init(SubScreenConfig config, boolean debugMode) {
        instance = new SubScreenManager(config, debugMode);
    }

    public static SubScreenManager getInstance() {
        return instance;
    }

    public static void open(Context context) {
        if (PurchaseHelper.getInstance().isRemovedAds(context)) {
            return;
        }
        SubActivity.open(context);
    }

    public static boolean isTestConsumePurchaseMode() {
        return SubConfigPrefs.get().isConsumePurchaseTest();
    }

    public String getSubDefaultPack() {
        return SubConfigPrefs.get().getDefaultSubPack();
    }

    public static void loadRewardAdIfPossible(Activity mainActivity) {
        try {
            if (PurchaseHelper.getInstance().isRemovedAds(mainActivity) || !SubConfigPrefs.get().isEnableRewardAds()) {
                return;
            }
            getInstance().getConfig().getRewardAdDelegate().loadAd(mainActivity);
        } catch (Exception e) {
            SubLogUtils.logE(e);
        }
    }

    public static Activity getMainActivity() {
        return mainActivity;
    }

    public static void destroyActivityInstance() {
        mainActivity = null;
    }

    public static void open(Context context, boolean enableRewardAds) {
        SubActivity.open(context, enableRewardAds);
    }

    public static void openWithRewardAdOnFinishIfPossible(Context context) {
        if (PurchaseHelper.getInstance().isRemovedAds(context)) {
            return;
        }
        SubActivity.open(context, SubConfigPrefs.get().isEnableRewardAds());
    }

    public static boolean isEnableSub(String position) {
        return SubConfigPrefs.get().isEnableSubScreen(position);
    }

    public static boolean openSubScreenWithRemoteConfigName(Context context, String positionName) {
        if (PurchaseHelper.getInstance().isRemovedAds(context)) {
            return false;
        }

        if (!SubConfigPrefs.get().isEnableSubScreen(positionName)) {
            return false;
        }

        open(context, SubConfigPrefs.get().isEnableRewardAds());
        return true;
    }

    public static boolean hasFirstOpenLog(String position, String type) {
        String key = position + "_" + type + "_first";
        return SubConfigPrefs.get().contains(key);
    }

    public static boolean hasFirstOpenLog(String position) {
        String key = position + "_first";
        return SubConfigPrefs.get().contains(key);
    }

    public static boolean openSubWithRemoteConfigNameAfterFirst(Context context, String position) {
        return openSubWithRemoteConfigNameAfterFirst(context, position, null);
    }

    public static boolean openSubWithRemoteConfigNameAfterFirst(Context context, String position, String type) {
        if (PurchaseHelper.getInstance().isRemovedAds(context)) {
            return false;
        }
        if (!SubConfigPrefs.get().isEnableSubScreen(position)) {
            return false;
        }
        String key = position + (TextUtils.isEmpty(type) ? "" : "_" + type) + "_first";
        boolean showFirst = SubConfigPrefs.get().getBoolean(key, true);
        if (showFirst) {
            SubConfigPrefs.get().putBoolean(key, false);
            return false;
        }
        open(context, SubConfigPrefs.get().isEnableRewardAds());
        return true;
    }

    public static boolean openSubSplash(Context context) {
        if (PurchaseHelper.getInstance().isRemovedAds(context) || !SubDeviceUtil.isConnected(context)) {
            return false;
        }

        if (!isEnableDefaultSub() && !SubConfigPrefs.get().hasSubStylesData()) {
            return false;
        }

        if (!SubConfigPrefs.get().isEnableSubSplash()) {
            return false;
        }
        open(context, false);
        return true;
    }

    private static boolean isEnableDefaultSub() {
        String currentCountryCode = SubConfigPrefs.get().getCurrentCountryCode().toUpperCase(Locale.US);
        if (TextUtils.isEmpty(currentCountryCode)) {
            return true;
        }

        String[] countryUseDefaultSub = getInstance().getConfig().getCountryUseDefaultSub();
        if (countryUseDefaultSub != null) {
            List<String> strings = Arrays.asList(countryUseDefaultSub);
            return strings.contains(currentCountryCode);
        }
        return false;
    }

    public SubScreenConfig getConfig() {
        return config;
    }

    public boolean isDebugMode() {
        return debugMode;
    }
}
