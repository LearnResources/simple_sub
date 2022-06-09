package com.petprojects.sub;

import java.util.List;

public class PurchaseConfig {
    private List<String> lifeTimePack;
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

    public List<String> getLifeTimePack() {
        return lifeTimePack;
    }

    public PurchaseConfig setLifeTimePack(List<String> lifeTimePack) {
        this.lifeTimePack = lifeTimePack;
        return this;
    }
}
