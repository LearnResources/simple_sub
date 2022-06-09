package com.sub.entities;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SubFeatureTexts {

    @SerializedName("featureTexts")
    private List<String> featureTexts;

    public List<String> getFeatureTexts() {
        return featureTexts;
    }
}