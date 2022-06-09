package com.sub;

import android.content.Context;

import androidx.fragment.app.Fragment;

public interface SubScreen {
    Fragment getFragment(Context context);
}
