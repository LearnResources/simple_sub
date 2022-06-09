package com.sub;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

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
import com.sub.event.PurchasedEvent;
import com.sub.util.SubLogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PurchaseHelper {
    private BillingClient billingClient;
    private PurchaseCallback callback;
    private static PurchaseHelper instance;
    private List<SkuDetails> skuDetailsListIAP = new ArrayList<>();
    private List<SkuDetails> skuDetailsListSUB = new ArrayList<>();

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
            if (callback != null) {
                callback.purchaseFail();
            }
        }
    };

    void handlePurchase(Purchase purchase) {
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
        if (EventBus.getDefault().hasSubscriberForEvent(PurchasedEvent.class)) {
            EventBus.getDefault().post(new PurchasedEvent());
        }
        SubConfigPrefs.get().updateLocalPurchasedState(true);
        if (callback != null) {
            callback.purchaseSuccessfully();
        }
    }

    public static synchronized PurchaseHelper getInstance() {
        if (instance == null || instance.billingClient == null) {
            instance = new PurchaseHelper();
        }
        return instance;
    }

    private PurchaseHelper() {
    }

    public void initBilling(Context context) {
        initBilling(context, null);
    }

    public void initBilling(final Context context, BillingClientStateListener billingClientStateListener) {
        billingClient = BillingClient.newBuilder(context)
                .setListener(purchasesUpdatedListener)
                .enablePendingPurchases()
                .build();
        billingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
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

    private void onBillingConnected(@NonNull BillingResult billingResult) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            PurchaseConfig purchaseConfig = getPurchaseConfig();
            if (purchaseConfig == null) {
                return;
            }

            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(Collections.singletonList(purchaseConfig.getLifeTimePack()))
                    .setType(BillingClient.SkuType.INAPP);
            billingClient.querySkuDetailsAsync(params.build(),
                    (billingResult12, skuDetailsList) -> {
                        PurchaseHelper.this.skuDetailsListIAP = skuDetailsList;
                        SubLogUtils.logD(billingResult12.getResponseCode());
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
        try {
            return SubScreenManager.getInstance().getConfig().getPurchaseConfig();
        } catch (Exception exception) {
            SubLogUtils.logE(exception);
        }
        return null;
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
        if (billingClient == null) {
            initBilling(context);
        }
        return isPurchased() || isSubscribed() || isRemovedAdsLocalState(context);
    }

    public boolean isRemovedAdsLocalState(Context context) {
        SubConfigPrefs subConfigPrefs = SubConfigPrefs.get();
        return subConfigPrefs.isLocalSubscribedState() || subConfigPrefs.isLocalPurchasedState();
    }

    private boolean isPurchased() {
        try {
            Purchase.PurchasesResult purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP);
            for (Purchase purchase : Objects.requireNonNull(purchasesResult.getPurchasesList())) {
                if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                    SubConfigPrefs.get().updateLocalPurchasedState(true);
                    return true;
                }
            }
            SubConfigPrefs.get().updateLocalPurchasedState(false);
        } catch (Exception exception) {
            SubLogUtils.logE(exception);
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
                        SubConfigPrefs.get().updateLocalPurchasedState(subscribed);
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
                SubConfigPrefs.get().updateLocalPurchasedState(purchased);
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
                    SubConfigPrefs.get().updateLocalSubscribedState(true);
                    return true;
                }
            }
            SubConfigPrefs.get().updateLocalSubscribedState(false);
        } catch (Exception exception) {
            SubLogUtils.logE(exception);
        }
        return false;
    }

    public void purchase(Activity activity, String productId) {
        try {
            SubLogUtils.logD(productId);
            if (billingClient == null) {
                initBilling(activity);
            }
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
        if (billingClient == null) {
            initBilling(activity);
        }
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

    public boolean isReady() {
        String price = PurchaseHelper.getInstance().getPrice(SubScreenManager.getInstance().getSubDefaultPack());
        return !TextUtils.isEmpty(price) && !price.equalsIgnoreCase("0$");
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
}
