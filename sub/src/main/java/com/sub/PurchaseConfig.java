package com.sub;

import java.util.List;

public class PurchaseConfig {
    private String lifeTimePack;
    private List<String> subPacks;

    PurchaseConfig() {
    }

    public static PurchaseConfig newConfig() {
        return new PurchaseConfig();
    }

    public List<String> getSubPacks() {
        return subPacks;
    }

    public PurchaseConfig setSubPacks(List<String> subPacks) {
        this.subPacks = subPacks;
        return this;
    }

    public String getLifeTimePack() {
        return lifeTimePack;
    }

    public PurchaseConfig setLifeTimePack(String lifeTimePack) {
        this.lifeTimePack = lifeTimePack;
        return this;
    }
}
