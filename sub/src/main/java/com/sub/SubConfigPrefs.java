package com.sub;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sub.base.BasePrefData;
import com.sub.entities.SubFeatureTexts;
import com.sub.util.RemoteConfigFetcher;
import com.sub.util.SubLogUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubConfigPrefs extends BasePrefData {
    private static final String K_SUB_STYLES = "sub_styles";

    private static final String K_ENABLE_SUB_SPLASH = "enable_sub_splash";
    private static final String K_ENABLE_REWARD_ADS = "enable_sub_reward_ads";
    private static final String K_SUB_SCREEN_CONFIG_PREFIX = "enable_sub_";
    private static final String K_SUB_CONFIG_PREFIX = "sub_";
    private static final String K_SUB_FEATURE_TEXT = "sub_feature_text";
    private static final String K_ENABLE_SUB_SOUND = "enable_sub_sound";
    private static final String K_ENABLE_SUB_SHOW_COUNT_DOWN = "enable_sub_show_count_down";

    private static final String K_CURRENT_COUNTRY_CODE = "current_country_code";
    private static final String K_LOCAL_PURCHASED = "local_purchased";
    private static final String K_LOCAL_SUBSCRIBED = "local_subscribed";
    private static final String K_SUB_TEST_CONSUME_PURCHASE = "sub_test_consume_purchase";
    private static final String K_SUB_DEFAULT_PACK = "sub_default_pack";

    private static SubConfigPrefs instance;

    private SubConfigPrefs(Context context) {
        super(context, "sub_configs");
        fetch();
    }

    public void fetch() {
        RemoteConfigFetcher.getInstance().fetch(new RemoteConfigFetcher.OnFetchListener() {
            @Override
            public void onSuccess(FirebaseRemoteConfig firebaseRemoteConfig) {
                saveInfo(firebaseRemoteConfig);
            }

            @Override
            public void onFail() {
            }
        });
    }

    private void saveInfo(FirebaseRemoteConfig firebaseRemoteConfig) {
        saveConfigByPrefix(firebaseRemoteConfig, K_SUB_SCREEN_CONFIG_PREFIX);
        saveConfigByPrefix(firebaseRemoteConfig, K_SUB_CONFIG_PREFIX);
    }

    private void saveConfigByPrefix(FirebaseRemoteConfig firebaseRemoteConfig, String prefix) {
        Set<String> subConfigKeys = firebaseRemoteConfig.getKeysByPrefix(prefix);
        SubLogUtils.logD(subConfigKeys.toString());
        for (String subConfigKey : subConfigKeys) {
            saveConfigs(firebaseRemoteConfig, subConfigKey);
        }
    }

    private void saveConfigs(FirebaseRemoteConfig firebaseRemoteConfig, String subConfigKey) {
        if (firebaseRemoteConfig == null) {
            return;
        }
        String inputStringFirebase = firebaseRemoteConfig.getString(subConfigKey);
        if (TextUtils.isEmpty(inputStringFirebase)) {
            return;
        }

        try {
            Pattern queryLangPattern = Pattern.compile("true|false", Pattern.CASE_INSENSITIVE);
            Matcher matcher = queryLangPattern.matcher(inputStringFirebase);
            if (matcher.matches()) {
                putBoolean(subConfigKey, Boolean.valueOf(inputStringFirebase));
                return;
            }
            throw new Exception("Invalid type");
        } catch (Exception exception) {
            try {
                putInt(subConfigKey, Integer.parseInt(inputStringFirebase));
            } catch (NumberFormatException e) {
                putString(subConfigKey, inputStringFirebase);
            }
        }
    }

    public void showAllData() {
        Map<String, ?> allEntries = pref().getAll();
        SubLogUtils.logD("==================================");
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            SubLogUtils.logD(entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    public boolean hasSubStylesData() {
        String data = getString(K_SUB_STYLES, null);
        return !TextUtils.isEmpty(data);
    }

    public List<String> getSubStyleOrderList() {
        String useSubStyles = getString(K_SUB_STYLES, null);
        SubLogUtils.logD(useSubStyles);
        if (TextUtils.isEmpty(useSubStyles)) {
            String[] subStyleDefault = null;
            try {
                subStyleDefault = SubScreenManager.getInstance().getConfig().getSubStyleDefault();
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (subStyleDefault != null) {
                return Arrays.asList(subStyleDefault);
            }
            return new ArrayList<>();
        }
        useSubStyles = useSubStyles.trim();
        if (useSubStyles.contains(",")) {
            List<String> results = new ArrayList<>();
            String[] split = useSubStyles.split(",");
            for (String style : split) {
                results.add(style.trim());
            }
            return results;
        }
        return Collections.singletonList(useSubStyles);
    }

    public static void init(Context context) {
        SubLogUtils.showCurrentMethodName();
        instance = new SubConfigPrefs(context);
    }

    public static SubConfigPrefs get() {
        if (instance == null) {
            throw new NullPointerException("Initialization require!");
        }
        return instance;
    }

    public boolean isEnableRewardAds() {
        return getBoolean(K_ENABLE_REWARD_ADS, false);
    }

    public boolean isEnableSubSplash() {
        return getBoolean(K_ENABLE_SUB_SPLASH, true);
    }

    public boolean isEnableSubScreen(String key) {
        return getBoolean(key, true);
    }

    public boolean isEnableSubSound() {
        return getBoolean(K_ENABLE_SUB_SOUND, true);
    }

    public boolean isEnableSubShowCountDown() {
        return getBoolean(K_ENABLE_SUB_SHOW_COUNT_DOWN, false);
    }

    public boolean contains(String key) {
        return pref().contains(key);
    }

    public void saveCurrentCountryCode(String countryCode) {
        putString(K_CURRENT_COUNTRY_CODE, countryCode);
    }

    public String getCurrentCountryCode() {
        return getString(K_CURRENT_COUNTRY_CODE, "");
    }

    public SubFeatureTexts getSubFeatureTexts() {
        try {
            String jsonData = getString(K_SUB_FEATURE_TEXT, null);
            return new Gson().fromJson(jsonData, SubFeatureTexts.class);
        } catch (JsonSyntaxException e) {
            return new SubFeatureTexts();
        }
    }

    public void updateLocalPurchasedState(boolean purchased) {
        putBoolean(K_LOCAL_PURCHASED, purchased);
    }

    public boolean isConsumePurchaseTest() {
        return getBoolean(K_SUB_TEST_CONSUME_PURCHASE, false);
    }

    public boolean isLocalPurchasedState() {
        return getBoolean(K_LOCAL_PURCHASED, false);
    }

    public void updateLocalSubscribedState(boolean purchased) {
        putBoolean(K_LOCAL_SUBSCRIBED, purchased);
    }

    public boolean isLocalSubscribedState() {
        return getBoolean(K_LOCAL_SUBSCRIBED, false);
    }

    public String getDefaultSubPack() {
        return getString(K_SUB_DEFAULT_PACK, null);
    }
}
