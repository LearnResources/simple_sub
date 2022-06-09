package com.sub.example.sub;

import android.content.Context;
import android.view.View;

import androidx.fragment.app.Fragment;

import com.petprojects.sub.SubScreen;
import com.sub.example.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sub1Fragment extends BaseAppSubFragment implements SubScreen {
    @Override
    public Fragment getFragment(Context context) {
        return new Sub1Fragment();
    }

    @Override
    protected void initViews(View rootView) {
        super.initViews(rootView);
    }

    @Override
    public int getActionButtonText() {
        return R.string.subscribe;
    }

    @Override
    public List<String> getSubFeatureTexts() {
        ArrayList<String> strings = new ArrayList<>();
        for (Integer featureText : Sub.featureTexts) {
            try {
                strings.add(getContext().getString(featureText));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return strings;
    }

    @Override
    protected int onLayout() {
        return R.layout.fragment_sub_1;
    }

    @Override
    protected int getVideoResourceId() {
        return -1;
    }

    @Override
    protected List<Integer> getCoverImages() {
        return Collections.emptyList();
    }
}
