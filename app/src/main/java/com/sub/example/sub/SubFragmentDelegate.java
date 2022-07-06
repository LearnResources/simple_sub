package com.sub.example.sub;

public interface SubFragmentDelegate {
    void onBackPressed();

    void finish();

    boolean showNextFragment();
}
