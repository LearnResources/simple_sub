package com.sub.example.sub;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;


import com.petprojects.sub.SubScreen;
import com.sub.example.R;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Sub2Fragment extends BaseAppSubFragment implements SubScreen {

    @Override
    public Fragment getFragment(Context context) {
        return new Sub2Fragment();
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);
    }

    @Override
    protected int getVideoResourceId() {
        return -1;
    }

    @Override
    public List<String> getSubFeatureTexts() {
        return Collections.emptyList();
    }

    @Override
    protected List<Integer> getCoverImages() {
        return Arrays.asList(
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher,
                R.mipmap.ic_launcher
        );
    }

    @Override
    protected boolean isTrialFragment() {
        return false;
    }

    @Override
    protected boolean isPackAllCap() {
        return false;
    }

    @Override
    public int getActionButtonText() {
        return R.string.subscribe;
    }

    @Override
    protected int onLayout() {
        return R.layout.fragment_sub_2;
    }
}
