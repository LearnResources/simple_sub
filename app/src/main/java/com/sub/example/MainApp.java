package com.sub.example;

import static com.sub.example.sub.Sub.PACK_LIFE_TIME;
import static com.sub.example.sub.Sub.PACK_SUB_MONTH;
import static com.sub.example.sub.Sub.PACK_SUB_WEEK;
import static com.sub.example.sub.Sub.PACK_SUB_YEAR;

import android.app.Application;

import com.petprojects.sub.PurchaseConfig;
import com.petprojects.sub.PurchaseHelper;
import com.petprojects.sub.util.SubPref;
import com.sub.example.sub.Sub;

import java.util.Arrays;
import java.util.Collections;

public class MainApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        SubPref.init(this);
        PurchaseConfig purchaseConfig = PurchaseConfig.newConfig();
        purchaseConfig.setLifeTimePack(Collections.singletonList(PACK_LIFE_TIME))
                .setSubPacks(Arrays.asList(PACK_SUB_WEEK, PACK_SUB_MONTH, PACK_SUB_YEAR));
        PurchaseHelper.getInstance().setPurchaseConfig(purchaseConfig);
        PurchaseHelper.getInstance().initBilling(this);
        Sub.init(this);
    }
}
