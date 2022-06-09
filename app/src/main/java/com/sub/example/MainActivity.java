package com.sub.example;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.petprojects.sub.SubScreenManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.bt_show_sub).setOnClickListener(view -> {
            SubScreenManager.open(this);
        });
    }
}