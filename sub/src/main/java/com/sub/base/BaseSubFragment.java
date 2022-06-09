package com.sub.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.sub.PurchaseCallback;
import com.sub.PurchaseHelper;
import com.sub.SubConfigPrefs;
import com.sub.SubFragmentDelegate;
import com.sub.SubScreen;
import com.sub.SubScreenManager;
import com.sub.entities.SubFeatureTexts;
import com.sub.util.SubLogUtils;

import java.util.List;

public abstract class BaseSubFragment extends Fragment
        implements PurchaseCallback, SubScreen {
    private SubFragmentDelegate delegate;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(onLayout(), container, false);
        initViews(rootView);
        return rootView;
    }

    public PurchaseHelper getPurchaseHelper() {
        return PurchaseHelper.getInstance().setCallback(this);
    }

    protected abstract void initViews(View rootView);

    protected abstract int onLayout();

    public void subscription(String subPack) {
        getPurchaseHelper().subscribe(requireActivity(), subPack);
    }

    public void buyLifetime(String pack) {
        getPurchaseHelper().purchase(requireActivity(), pack);
    }

    public boolean isSub(String pack) {
        return getPurchaseHelper().isSub(pack);
    }

    public void skip() {
        if (delegate != null) {
            delegate.finish();
        }
    }

    public boolean close() {
        SubLogUtils.showCurrentMethodName();
        if (delegate == null) {
            return false;
        }
        return delegate.showNextFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SubFragmentDelegate) {
            delegate = (SubFragmentDelegate) context;
        }
    }

    @Override
    public void purchaseFail() {
        close();
    }

    @Override
    public void purchaseSuccessfully() {
        if (delegate != null) {
            delegate.finish();
        }
    }

    public List<String> getSubFeatureTexts() {
        List<String> subDefaultFeatureTexts = SubScreenManager.getInstance().getConfig().getSubDefaultFeatureTexts();
        try {
            SubFeatureTexts subFeatureTexts = SubConfigPrefs.get().getSubFeatureTexts();
            List<String> featureTexts = subFeatureTexts.getFeatureTexts();
            if (featureTexts == null || featureTexts.isEmpty()) {
                return subDefaultFeatureTexts;
            }
            return featureTexts;
        } catch (Exception e) {
            e.printStackTrace();
            return subDefaultFeatureTexts;
        }
    }

}
