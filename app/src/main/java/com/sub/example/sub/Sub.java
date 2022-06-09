package com.sub.example.sub;

import android.content.Context;
import android.text.TextUtils;

import com.petprojects.sub.PurchaseConfig;
import com.petprojects.sub.PurchaseHelper;
import com.petprojects.sub.SubScreen;
import com.petprojects.sub.SubScreenConfig;
import com.petprojects.sub.SubScreenManager;
import com.sub.example.BuildConfig;
import com.sub.example.R;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Sub {
    public static final String PACK_LIFE_TIME = BuildConfig.DEBUG ? "android.test.purchased" : "pack_life_time";
    public static final String PACK_SUB_WEEK = "pack_sub_week";
    public static final String PACK_SUB_MONTH = "pack_sub_month";
    public static final String PACK_SUB_YEAR = "pack_sub_year";

    private static final String PACK_DEFAULT = PACK_SUB_WEEK;
    public static final String PACK_BEST_OFFER = PACK_SUB_WEEK;

    public static final String SUB_SCREEN_1 = "sub_1";
    public static final String SUB_SCREEN_2 = "sub_2";
    public static final String DEFAULT_PRIVACY_COLOR = "#246FFF";

    public static String getPackDefault() {
        try {
            String remoteSubDefaultPack = SubScreenManager.getInstance().getSubDefaultPack();
            if (TextUtils.isEmpty(remoteSubDefaultPack)) {
                return PACK_DEFAULT;
            }
            return remoteSubDefaultPack;
        } catch (Exception e) {
            return PACK_DEFAULT;
        }
    }

    private static final List<PurchasePack> purchasePacks = Arrays.asList(
            PurchasePack.newPack("Get Your 3-Days Free", Sub.PACK_SUB_MONTH, R.string.sub_monthly_desc),
            PurchasePack.newPack("3-Day free trial", Sub.PACK_SUB_WEEK, R.string.sub_day_desc),
            PurchasePack.newPack("Lifetime", Sub.PACK_LIFE_TIME));

    private static final List<PurchasePack> purchasePacksYearly = Arrays.asList(
            PurchasePack.newPack("Weekly", Sub.PACK_SUB_WEEK, R.string.sub_day_desc),
            PurchasePack.newPack("3 - Days\nFree trial", Sub.PACK_SUB_MONTH, R.string.sub_monthly_desc),
            PurchasePack.newPack("Lifetime", Sub.PACK_LIFE_TIME),
            PurchasePack.newPack("Yearly", Sub.PACK_SUB_YEAR, R.string.sub_year_desc));

    private static PurchaseHelper getPurchaseHelper() {
        return PurchaseHelper.getInstance();
    }

    public static final List<Integer> featureTexts = Arrays.asList(
            R.string.sub_feature_text_1,
            R.string.sub_feature_text_2,
            R.string.sub_feature_text_3,
            R.string.sub_feature_text_4
    );

    public static List<PurchasePack> getPurchasePacksYearly() {
        return purchasePacksYearly;
    }

    public static List<PurchasePack> getPurchasePacks() {
        return purchasePacksYearly;
    }

    public static void init(Context context) {
        Map<String, SubScreen> subActivityMap = new LinkedHashMap<>();
        subActivityMap.put(SUB_SCREEN_1, new Sub1Fragment());
        subActivityMap.put(SUB_SCREEN_2, new Sub2Fragment());

        PurchaseConfig purchaseConfig = PurchaseConfig.newConfig();
        purchaseConfig.setLifeTimePack(Arrays.asList(PACK_LIFE_TIME))
                .setSubPacks(Arrays.asList(PACK_SUB_WEEK, PACK_SUB_MONTH, PACK_SUB_YEAR));

        SubScreenConfig config = SubScreenConfig.newConfig(context)
                .setSubStyles(subActivityMap)
                .setDefaultSubFeatureTexts(featureTexts)
                .setSubStyleDefault(SUB_SCREEN_1, SUB_SCREEN_2)
                .setPurchaseConfig(purchaseConfig);

        SubScreenManager.init(config);
    }


    public static boolean isPurchased(Context context) {
        return PurchaseHelper.getInstance().isRemovedAds(context);
    }
}
