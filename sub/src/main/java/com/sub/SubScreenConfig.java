package com.sub;

import android.content.Context;

import androidx.fragment.app.Fragment;

import com.sub.util.SubLogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SubScreenConfig {
    private Map<String, SubScreen> subStyles;
    private final Context context;
    private SubRewardAdDelegate rewardAdDelegate;
    private PurchaseConfig purchaseConfig;
    private boolean enableBack;
    private String[] subStyleDefault;
    private String[] countryUseDefaultSub;
    private List<Integer> subDefaultFeatureTexts;

    public Context getContext() {
        return context;
    }

    SubScreenConfig(Context context) {
        this.context = context;
    }

    public static SubScreenConfig newConfig(Context context) {
        return new SubScreenConfig(context);
    }

    public Map<String, SubScreen> getSubStyles() {
        return subStyles;
    }

    public ArrayList<Fragment> getOrderFragments() {
        Map<String, SubScreen> subStyles = getSubStyles();
        if (subStyles == null || subStyles.isEmpty()) {
            return new ArrayList<>();
        }
        ArrayList<Fragment> fragments = new ArrayList<>();
        List<String> subStyleOrderList = SubConfigPrefs.get().getSubStyleOrderList();
        SubLogUtils.logD(String.valueOf(subStyleOrderList));
        int size = subStyleOrderList.size();
        for (int i = 0; i < size; i++) {
            SubScreen subScreen = subStyles.get(subStyleOrderList.get(i));
            if (subScreen != null) {
                fragments.add(subScreen.getFragment(context));
            }
        }
        return fragments;
    }

    public SubScreenConfig setSubStyleDefault(String... subStyleDefault) {
        this.subStyleDefault = subStyleDefault;
        return this;
    }

    public String[] getSubStyleDefault() {
        return subStyleDefault;
    }

    public SubScreenConfig setSubStyles(Map<String, SubScreen> subStyles) {
        this.subStyles = subStyles;
        return this;
    }

    public SubScreenConfig enableBack() {
        this.enableBack = true;
        return this;
    }

    public boolean isEnableBack() {
        return enableBack;
    }

    public SubRewardAdDelegate getRewardAdDelegate() {
        if (this.rewardAdDelegate != null) {
            return rewardAdDelegate;
        }
        return null;
    }

    public SubScreenConfig setRewardAdDelegate(SubRewardAdDelegate rewardAdDelegate) {
        this.rewardAdDelegate = rewardAdDelegate;
        return this;
    }

    public PurchaseConfig getPurchaseConfig() {
        return purchaseConfig;
    }

    public SubScreenConfig setPurchaseConfig(PurchaseConfig purchaseConfig) {
        this.purchaseConfig = purchaseConfig;
        return this;
    }

    public SubScreenConfig setCountryCodeListEnableStyleDefault(String[] countryUseDefaultSub) {
        this.countryUseDefaultSub = countryUseDefaultSub;
        return this;
    }

    public String[] getCountryUseDefaultSub() {
        return countryUseDefaultSub;
    }

    public SubScreenConfig setDefaultSubFeatureTexts(List<Integer> subFeatureTexts) {
        this.subDefaultFeatureTexts = subFeatureTexts;
        return this;
    }

    public List<String> getSubDefaultFeatureTexts() {
        if (this.subDefaultFeatureTexts == null || this.subDefaultFeatureTexts.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> results = new ArrayList<>();
        for (Integer subDefaultFeatureText : subDefaultFeatureTexts) {
            results.add(context.getString(subDefaultFeatureText));
        }
        return results;
    }
}
