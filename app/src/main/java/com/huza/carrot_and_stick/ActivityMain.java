package com.huza.carrot_and_stick;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;

public class ActivityMain extends AppCompatActivity {

    private static final String PACKAGE_NAME = "Carrot_and_Stick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseCrash.report(new Exception("My first Android non-fatal error"));
    }

    @Override
    protected void onStart() {
        super.onStart();

        first_checkPermission();
    }

    public void first_checkPermission(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    Thread.sleep(1000);

                    Boolean callphone = false, deviceadmin = false, overlay = false;

                    if (hasCallPhonePermission()) callphone = true;
                    if (hasDeviceAdminPermission()) deviceadmin = true;
                    if (hasWindowOverlayPermission()) overlay = true;

                    Log.i(PACKAGE_NAME, "ActivityMain : callphone = " + callphone + "    deviceadmin = " + deviceadmin + "    overlay = " + overlay);

                    if (deviceadmin&&overlay&&callphone){
                        startActivity(new Intent(getApplicationContext(), ActivityLogin.class));
                    } else {
                        startActivity(new Intent(getApplicationContext(), ActivityPermission.class));
                    }

                    finish();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public boolean hasDeviceAdminPermission() {
        ComponentName adminComponent = new ComponentName(getApplicationContext(), ReceiverDeviceAdmin.class);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        if (devicePolicyManager.isAdminActive(adminComponent))
            return true;
        else
            return false;
    }
    public boolean hasWindowOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!Settings.canDrawOverlays(getApplicationContext()))
                return false;
        }

        return true;
    }
    public boolean hasCallPhonePermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return false;
            }
        }

        return true;
    }
}
