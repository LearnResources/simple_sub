package com.sub.util;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

public class RemoteConfigFetcher {
    private static RemoteConfigFetcher instance;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    public static RemoteConfigFetcher getInstance() {
        if (instance == null) {
            instance = new RemoteConfigFetcher();
        }
        return instance;
    }

    public void fetch(OnFetchListener onFetchListener) {

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(0)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(task -> {
                    SubLogUtils.logD("Fetch configs successfully");
                    if (task.isSuccessful()) {
                        firebaseRemoteConfig.activate();

                        if (onFetchListener != null) {
                            onFetchListener.onSuccess(firebaseRemoteConfig);
                        }
                        return;
                    }
                    if (onFetchListener != null) {
                        onFetchListener.onFail();
                    }
                }).addOnFailureListener(e -> {
            SubLogUtils.logE(e);
            e.printStackTrace();
        });
    }

    public void reset() {
        try {
            this.firebaseRemoteConfig.reset();
            this.fetch();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fetch() {
        fetch(null);
    }

    public interface OnFetchListener {
        void onSuccess(FirebaseRemoteConfig firebaseRemoteConfig);

        void onFail();
    }
}
