package com.petprojects.sub;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.ConsumeParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.petprojects.sub.util.SubLogUtils;
import com.petprojects.sub.util.SubPref;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PurchaseHelper {
    private BillingClient billingClient;
    private PurchaseCallback callback;
    private static PurchaseHelper instance;
    private List<SkuDetails> skuDetailsListIAP = new ArrayList<>();
    private List<SkuDetails> skuDetailsListSUB = new ArrayList<>();
    private SubBillingPriceCallback subBillingPriceCallback;
    private PurchaseConfig purchaseConfig;

    public PurchaseHelper setCallback(PurchaseCallback callback) {
        this.callback = callback;
        return this;
    }

    private final PurchasesUpdatedListener purchasesUpdatedListener = new PurchasesUpdatedListener() {

        public void onPurchasesUpdated(BillingResult billingResult, List<Purchase> purchases) {
            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK
                    && purchases != null) {
                for (int i = 0; i < purchases.size(); i++) {
                    handlePurchase(purchases.get(i));
                }
                return;
            }
//            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//                SubAppChecker.instance().getAppDelegate().registerNotificationTopic(SubCheckerTopic.SUB_CANCEL);
//            }
            if (callback != null) {
                callback.purchaseFail();
            }
        }
    };

    void handlePurchase(Purchase purchase) {
        Log.i("superman", "handlePurchase: " + purchase.getPurchaseState());
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (purchase.isAcknowledged()) {
                return;
            }

            AcknowledgePurchaseParams acknowledgePurchaseParams =
                    AcknowledgePurchaseParams.newBuilder()
                            .setPurchaseToken(purchase.getPurchaseToken())
                            .build();
            billingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {
                purchaseSuccess();
            });
        }
    }

    public void purchaseSuccess() {
//        SubAppChecker.instance().getAppDelegate()
//                .unregisterNotificationTopic(SubCheckerTopic.SUB_CANCEL);
//
//        if (EventBus.getDefault().hasSubscriberForEvent(PurchasedEvent.class)) {
//            EventBus.getDefault().post(new PurchasedEvent());
//        }
        SubPref.get().updateLocalPurchasedState(true);
        if (callback != null) {
            callback.purchaseSuccessfully();
        }
    }

    public static synchronized PurchaseHelper getInstance() {
        if (instance == null) {
            instance = new PurchaseHelper();
        }
        return instance;
    }

    public PurchaseHelper setPurchaseConfig(PurchaseConfig purchaseConfig) {
        Log.i("superman", "setPurchaseConfig: " + purchaseConfig);
        this.purchaseConfig = purchaseConfig;
        return this;
    }

    private PurchaseHelper() {
        Log.i("superman", "PurchaseHelper: new ins");
    }

    public CurrencyInfo getPriceValue(String productId) {
        try {
            String priceFormat = null;
            for (SkuDetails skuDetails : skuDetailsListSUB) {
                if (TextUtils.isEmpty(priceFormat)) {
                    priceFormat = keepUniqueString(skuDetails.getPrice().replaceAll("\\.", "")
                            .replaceAll(",", "")
                            .replaceAll("\\d", "%s"));
                }
                if (skuDetails.getSku().equals(productId)) {
                    return getCurrencyInfo(priceFormat, skuDetails);
                }
            }
            for (SkuDetails skuDetails : skuDetailsListIAP) {
                if (TextUtils.isEmpty(priceFormat)) {
                    priceFormat = keepUniqueString(skuDetails.getPrice().replaceAll("\\.", "")
                            .replaceAll(",", "")
                            .replaceAll("\\d", "%s"));
                }
                if (skuDetails.getSku().equals(productId)) {
                    return getCurrencyInfo(priceFormat, skuDetails);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private CurrencyInfo getCurrencyInfo(String priceFormat, SkuDetails skuDetails) {
        double price = (float) skuDetails.getPriceAmountMicros() / 1000000;
        return new CurrencyInfo(priceFormat, price, skuDetails.getPrice());
    }

    private String keepUniqueString(String priceFormat) {
        StringBuilder temp = new StringBuilder();
        for (int i = 0; i < priceFormat.length(); i++) {
            char ch = priceFormat.charAt(i);
            if (temp.toString().indexOf(ch) == -1) {
                temp.append(ch);
            } else {
                temp.toString().replace(String.valueOf(ch), ""); // added this to your existing code
            }
        }
        return temp.toString();
    }

    public void initBilling(Context context) {
        initBilling(context, null);
    }

    public void loadBillingPriceAsync(Context context, SubBillingPriceCallback subBillingPriceCallback) {
        this.subBillingPriceCallback = subBillingPriceCallback;
        if (hasPriceData()) {
            this.subBillingPriceCallback.onSuccess();
            return;
        }
        initBilling(context);
    }

    public void initBilling(final Context context, BillingClientStateListener billingClientStateListener) {
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                SubLogUtils.logD(billingResult.getResponseCode());
                onBillingConnected(billingResult);
                if (billingClientStateListener != null) {
                    billingClientStateListener.onBillingSetupFinished(billingResult);
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                SubLogUtils.showCurrentMethodName();
                if (billingClientStateListener != null) {
                    billingClientStateListener.onBillingServiceDisconnected();
                }
            }
        });
    }

    private void onBillingConnected(BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            PurchaseConfig purchaseConfig = getPurchaseConfig();
            if (purchaseConfig == null) {
                Log.i("superman", "onBillingConnected: nulled");
                return;
            }

            Log.i("superman", "onBillingConnected: " + purchaseConfig.getLifeTimePack());

            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(purchaseConfig.getLifeTimePack())
                    .setType(BillingClient.SkuType.INAPP);
            billingClient.querySkuDetailsAsync(params.build(),
                    (billingResult12, skuDetailsList) -> {
                        PurchaseHelper.this.skuDetailsListIAP = skuDetailsList;
                        SubLogUtils.logD(billingResult12.getResponseCode());
                        if (subBillingPriceCallback != null) {
                            subBillingPriceCallback.onSuccess();
                        }
                    });

            params.setSkusList(purchaseConfig.getSubPacks()).setType(BillingClient.SkuType.SUBS);
            billingClient.querySkuDetailsAsync(params.build(),
                    (billingResult1, skuDetailsList) -> {
                        PurchaseHelper.this.skuDetailsListSUB = skuDetailsList;
                        SubLogUtils.logD(billingResult1.getResponseCode());
                    });
        }
    }

    private PurchaseConfig getPurchaseConfig() {
        return purchaseConfig;
    }

    public void consume(Context context, String productId) {
        Purchase purchase = getPurchase(productId);
        if (purchase != null) {
            ConsumeParams consumeParams = ConsumeParams.newBuilder().setPurchaseToken(purchase.getPurchaseToken()).build();
            billingClient.consumeAsync(consumeParams, (billingResult, s) -> {
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                    fetchPurchasedAsync(null);
                }
            });
        }
    }

    private Purchase getPurchase(String productId) {
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase :
                    Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getSkus().contains(productId)) return purchase;
            }
        } catch (Exception exception) {
            SubLogUtils.logE(exception);
        }
        return null;
    }

    public boolean isRemovedAds(Context context) {
//        if (billingClient == null) {
//            initBilling(context);
//        }
        return isPurchased() || isSubscribed() || isRemovedAdsLocalState(context);
    }

    public boolean isRemovedAdsLocalState(Context context) {
        SubPref subConfigPrefs = SubPref.get();
        return subConfigPrefs.isLocalSubscribedState() || subConfigPrefs.isLocalPurchasedState();
    }

    private boolean isPurchased() {
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase : Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    SubPref.get().updateLocalPurchasedState(true);
                    return true;
                }
            }
            SubPref.get().updateLocalPurchasedState(false);
        } catch (Exception ignored) {
        }
        return false;
    }

    public void fetchPurchasedAsync(CheckPurchaseStateCallback callback) {
        billingClient.queryPurchasesAsync(BillingClient.SkuType.INAPP, (billingResult, list) -> {
            try {
                boolean purchased = false;
                for (Purchase purchase : Objects.requireNonNull(list)) {
                    if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                        purchased = true;
                    }
                }
                if (!purchased) {
                    billingClient.queryPurchasesAsync(BillingClient.SkuType.SUBS, (billingResult1, list1) -> {
                        boolean subscribed = false;
                        for (Purchase purchase : Objects.requireNonNull(list1)) {
                            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                                subscribed = true;
                            }
                        }
                        SubPref.get().updateLocalPurchasedState(subscribed);
                        if (callback != null) {
                            if (subscribed) {
                                callback.onPurchased();
                            } else {
                                callback.onAppNotPurchased();
                            }
                        }
                    });
                    return;
                }
                SubPref.get().updateLocalPurchasedState(purchased);
                if (callback != null) {
                    callback.onPurchased();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean isSubscribed() {
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.SUBS);
            for (Purchase purchase : Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    SubPref.get().updateLocalSubscribedState(true);
                    return true;
                }
            }
            SubPref.get().updateLocalSubscribedState(false);
        } catch (Exception exception) {
            SubLogUtils.logE(exception);
        }
        return false;
    }

    public void purchase(Activity activity, String productId) {
        try {
            SubLogUtils.logD(productId);
//            if (billingClient == null) {
//                initBilling(activity);
//            }
            SkuDetails skuDetails = getSkuDetail(skuDetailsListIAP, productId);
            BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                    .setSkuDetails(skuDetails)
                    .build();
            billingClient.launchBillingFlow(activity, billingFlowParams);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void subscribe(Activity activity, String productId) {
        SubLogUtils.logD(productId);
//        if (billingClient == null) {
//            initBilling(activity);
//        }
        SkuDetails skuDetails = getSkuDetail(skuDetailsListSUB, productId);
        BillingFlowParams billingFlowParams = BillingFlowParams.newBuilder()
                .setSkuDetails(skuDetails)
                .build();
        billingClient.launchBillingFlow(activity, billingFlowParams);
    }

    private SkuDetails getSkuDetail(List<SkuDetails> skuDetailsListSUB, String productId) {
        try {
            for (SkuDetails skuDetails : skuDetailsListSUB) {
                if (skuDetails.getSku().equals(productId)) {
                    return skuDetails;
                }
            }
        } catch (Exception e) {
            SubLogUtils.logE(e);
        }
        return null;
    }

    public boolean isSub(String pack) {
        try {
            return Objects.requireNonNull(getPurchaseConfig()).getSubPacks().contains(pack);
        } catch (Exception exception) {
            SubLogUtils.logE(exception);
        }
        return false;
    }

    public String getPrice(String productId) {
        String defaultPrice = "0$";
        if (billingClient == null || !billingClient.isReady()) {
            return defaultPrice;
        }
        try {
            for (SkuDetails skuDetails : skuDetailsListSUB) {
                if (skuDetails.getSku().equals(productId)) {
                    return skuDetails.getPrice();
                }
            }
            for (SkuDetails skuDetails : skuDetailsListIAP) {
                if (skuDetails.getSku().equals(productId)) {
                    return skuDetails.getPrice();
                }
            }
        } catch (Exception exception) {
            SubLogUtils.logE(exception);
        }
        return defaultPrice;
    }

    public boolean hasPriceData() {
        return skuDetailsListIAP != null && !skuDetailsListIAP.isEmpty()
                || skuDetailsListSUB != null && !skuDetailsListSUB.isEmpty();
    }
}
