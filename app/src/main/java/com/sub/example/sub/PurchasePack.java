package com.sub.example.sub;

import android.content.Context;

import androidx.annotation.StringRes;

import com.sub.PurchaseHelper;

public class PurchasePack {
    private final String packTitle;
    private final String pack;
    private int descFormatStringResourceId;

    PurchasePack(String packTitle, String pack, int descFormatStringResourceId) {
        this.packTitle = packTitle;
        this.pack = pack;
        this.descFormatStringResourceId = descFormatStringResourceId;
    }

    public static PurchasePack newPack(String packTitle, String pack, @StringRes int descFormatString) {
        return new PurchasePack(packTitle, pack, descFormatString);
    }

    public static PurchasePack newPack(String packTitle, String pack) {
        return new PurchasePack(packTitle, pack, -1);
    }

    public String getPackTitle() {
        return packTitle;
    }

    public String getPack() {
        return pack;
    }

    public String getDescString(Context context) {
        if (descFormatStringResourceId <= 0) {
            return getPrice();
        }
        return String.format(context.getString(descFormatStringResourceId), getPrice());
    }

    public String getPrice() {
        return PurchaseHelper.getInstance().getPrice(pack);
    }
}