package com.petprojects.sub;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.petprojects.sub.base.BasePrefData;
import com.petprojects.sub.checker.model.Response;
import com.petprojects.sub.entities.SubFeatureTexts;
import com.petprojects.sub.util.RemoteConfigFetcher;
import com.petprojects.sub.util.SubLogUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SubConfigPrefs extends BasePrefData {
    private static final String DEFAULT_SCRIPT_DAYS = "1, 3, 7";

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
    private static final String K_SUB_SCRIPT_CONFIGS = "sub_script_configs";
    private static final String K_SUB_SCRIPT_DAYS = "sub_script_days";
    private static final String K_ENABLE_SUB_SCRIPT = "enable_sub_script";
    private static final String K_SUB_SALE_TIME = "sub_sale_time";

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

        String subScriptConfigs = firebaseRemoteConfig.getString(K_SUB_SCRIPT_CONFIGS);
        if (!TextUtils.isEmpty(subScriptConfigs)) {
            SubConfigPrefs.get().putString(K_SUB_SCRIPT_CONFIGS, subScriptConfigs);
        }
        String subScriptDays = firebaseRemoteConfig.getString(K_SUB_SCRIPT_DAYS);
        if (!TextUtils.isEmpty(subScriptDays)) {
            SubConfigPrefs.get().putString(K_SUB_SCRIPT_DAYS, subScriptDays);
        }
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

    public List<Integer> getSubDays() {
        String subScriptDaysData = getString(K_SUB_SCRIPT_DAYS, DEFAULT_SCRIPT_DAYS);
        if (TextUtils.isEmpty(subScriptDaysData)) {
            return Collections.emptyList();
        }
        String[] split = subScriptDaysData.split(",");
        if (split.length <= 0) {
            return Collections.emptyList();
        }
        List<Integer> results = new ArrayList<>();
        for (String numberString : split) {
            if (TextUtils.isEmpty(numberString)) {
                continue;
            }
            results.add(Integer.parseInt(numberString.trim()));
        }
        return results;
    }

    public boolean isEnableSubScript() {
        return getBoolean(K_ENABLE_SUB_SCRIPT, true);
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

    public Response getScriptData() {
        //<editor-fold desc="fake data">
        String defaultData = "{\n" +
                "  \"script\": {\n" +
                "    \"lifecycle\": {\n" +
                "      \"app_open\": {\n" +
                "        \"number\": 3,\n" +
                "        \"name\": \"app_open\"\n" +
                "      }\n" +
                "    },\n" +
//                "    \"events\": [\n" +
//                "      {\n" +
//                "        \"name\": \"CLICK_OPEN_ITEM\"\n" +
//                "      },\n" +
//                "      {\n" +
//                "        \"name\": \"CAT_HOME_IMAGE\"\n" +
//                "      }\n" +
//                "    ],\n" +
                "    \"message\": [\n" +
                "      {\n" +
                "        \"title\": \"Limited Time Offer – Hurry Up!\",\n" +
                "        \"desc\": \"Super Sale is Live @ Up to 50 % Off\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"title\": \"So Low, So Good, It’s Going\",\n" +
                "        \"desc\": \"BIG SALE! Up to 50% Off @ Limited Time Only\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"title\": \"SPECIAL OFFER: Up to 50% Off!\",\n" +
                "        \"desc\": \"Our Products are the BEST, and the Price is LESS.\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"title\": \"Up to 50% Off SPECIAL SALE\",\n" +
                "        \"desc\": \"Luxurious Things at an Affordable Price.\"\n" +
                "      },\n" +
                "      {\n" +
                "        \"title\": \"Hurry Up! Limited Time Offer @ Up to 50% Off\",\n" +
                "        \"desc\": \"Don’t Buy from us Unless you’re not Ready for Success\"\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}";
        //</editor-fold>
        String data = getString(K_SUB_SCRIPT_CONFIGS, defaultData);
        if (TextUtils.isEmpty(data)) {
            return null;
        }
        Response response = null;
        try {
            response = new Gson().fromJson(data, Response.class);
        } catch (JsonSyntaxException e) {
            e.printStackTrace();
        }
        return response;
    }

    // dd/MM/yyyy HH:mm
    public long getSaleTime() {
        long time = 0;
        String saleTimeString = getString(K_SUB_SALE_TIME, null);
        if (!TextUtils.isEmpty(saleTimeString)) {
            time = getTimeMillsFromString(saleTimeString);
        }
        long totalTime = time - System.currentTimeMillis();
        if (totalTime > 0) {
            return time;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, 6);
        return calendar.getTimeInMillis();
    }

    private long getTimeMillsFromString(String timeString) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        long value = 0L;
        if (!TextUtils.isEmpty(timeString)) {
            try {
                value = Objects.requireNonNull(simpleDateFormat.parse(timeString)).getTime();
            } catch (Exception e) {
                try {
                    value = Objects.requireNonNull(simpleDateFormat.parse(timeString + " 00:00")).getTime();
                } catch (ParseException parseException) {
                    parseException.printStackTrace();
                }
            }
        }
        return value;
    }

}
