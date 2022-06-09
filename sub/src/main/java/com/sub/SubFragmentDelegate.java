package com.sub;

public interface SubFragmentDelegate {
    void onBackPressed();

    void finish();

    boolean showNextFragment();
}
