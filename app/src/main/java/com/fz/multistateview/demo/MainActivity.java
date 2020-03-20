package com.fz.multistateview.demo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.fz.multistateview.MultiStateView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MultiStateView multiStateView = findViewById(R.id.multi_state_view);
        multiStateView.showLoadingView();
    }
}
