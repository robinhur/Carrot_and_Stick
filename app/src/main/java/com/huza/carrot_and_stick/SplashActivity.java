package com.huza.carrot_and_stick;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    final String PACKAGE_NAME = "Carrot_and_Stick";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        final Thread myThread = new Thread(new Runnable() {
            public void run() {
                try {
                    Thread.sleep(1000);

                    Boolean overlay = false, deviceadmin = false;

                    if (hasWindowOverlayPermission(getApplicationContext())) overlay = true;

                    ComponentName adminComponent = new ComponentName(getApplicationContext(), DeviceAdmin_Receiver.class);
                    DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    if (devicePolicyManager.isAdminActive(adminComponent)) deviceadmin = true;

                    if (!(overlay&&deviceadmin)){
                        startActivity(new Intent(getApplicationContext(), PermissionActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        finish();
                    }


                    /// 그리기 권한 체크!!! ///
                    /*
                    if (!hasWindowOverlayPermission(getApplicationContext())){

                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                        Log.d(PACKAGE_NAME, "SplashActivity : overlay 권한 없음");
                        startActivity(intent);


                    } else {
                        //startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                        //finish();
                    }
                    ///////////////////////////

                    /// 기기관리자 권한 체크!!! ///
                    //ComponentName adminComponent = new ComponentName(getApplicationContext(), DeviceAdmin_Receiver.class);
                    //DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                    //if (!devicePolicyManager.isAdminActive(adminComponent)){

                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,adminComponent);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"앱을 사용하기 위해선 기기 관리자 권한이 활성화되어야 합니다.");
                        startActivity(intent);

                        // 권한 해제
                        // devicePolicyManager.removeActiveAdmin(adminComponent);

                    //} else {
                    //    Log.d(PACKAGE_NAME, "SplashActivity : device_admin 권한 있음");
                    //}
                    ///////////////////////////////
                    */

                } catch (Throwable t) {}
            }
        });

        myThread.start();

    }

    public static boolean hasWindowOverlayPermission(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(context)) {
                return false;
            }
            return true;
        }
        return true;
    }

}
