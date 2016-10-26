package com.huza.carrot_and_stick;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    private static final String PACKAGE_NAME = "Carrot_and_Stick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_clicked(null);
    }

    public void btn_clicked(View v) {

        Intent background_intent = new Intent(this, BackgroundService.class);
        startService(background_intent);
        finish();

    }
}
