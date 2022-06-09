package com.petprojects.sub;

public interface SubFragmentDelegate {
    void onBackPressed();

    void finish();

    boolean showNextFragment();
}
