package com.petprojects.sub;

import android.content.Context;
import android.text.TextUtils;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.petprojects.sub.util.SubLogUtils;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class SubGeoRequester {
    public interface Callback {
        void onSuccess(String countryCode);

        void onError(String error);
    }

    public static void fetch(Context context) {
        request(context, null);
    }

    public static void requestWithoutNetworkingRequestConfig(Callback callback) {
        AndroidNetworking.post("http://ip-api.com/json")
                .setPriority(Priority.HIGH)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        if (response == null) {
                            if (callback != null) {
                                callback.onError("No response");
                            }
                            return;
                        }
                        String countryCode = response.optString("countryCode");
                        if (TextUtils.isEmpty(countryCode)) {
                            if (callback != null) {
                                callback.onError("No country code");
                            }
                            return;
                        }
                        SubLogUtils.logD("Country code is " + countryCode);
                        SubConfigPrefs.get().saveCurrentCountryCode(countryCode);
                        if (callback != null) {
                            callback.onSuccess(countryCode);
                        }
                    }

                    @Override
                    public void onError(ANError error) {
                        SubLogUtils.logE(error);
                        if (callback != null) {
                            callback.onError(error.getMessage());
                        }
                    }
                });
    }

    public static void request(Context context, Callback callback) {
        SubScreenConfig config = SubScreenManager.getInstance().getConfig();
        if (config == null || config.getCountryUseDefaultSub() == null) {
            callback.onError("Sub default is turn off. Abort request!");
            return;
        }

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .build();

        AndroidNetworking.initialize(context, okHttpClient);
        requestWithoutNetworkingRequestConfig(callback);
    }
}
