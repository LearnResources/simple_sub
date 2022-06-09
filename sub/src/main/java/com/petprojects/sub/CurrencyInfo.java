package com.petprojects.sub;

public class CurrencyInfo {
    private String format;
    private double price;
    private String skuDetailsPrice;

    public CurrencyInfo(String format, double price, String skuDetailsPrice) {
        this.format = format;
        this.price = price;
        this.skuDetailsPrice = skuDetailsPrice;
    }

    public double getPrice() {
        return price;
    }

    public String getFormat() {
        return format;
    }

    public String getSkuDetailsPrice() {
        return skuDetailsPrice;
    }
}