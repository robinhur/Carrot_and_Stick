package com.huza.carrot_and_stick;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

public class PermissionActivity extends FragmentActivity {

    WelcomeAdapter adapter;
    ViewPager pager;
    DotIndicator indicator;

    Button btn_next;
    int now_pos = 0;

    ComponentName adminComponent;
    DevicePolicyManager devicePolicyManager;

    public void btn_next_clicked(View v) {
        switch (now_pos) {
            case 0:   // 0 -> 1로

                adminComponent = new ComponentName(getApplicationContext(), DeviceAdmin_Receiver.class);
                devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                if (!devicePolicyManager.isAdminActive(adminComponent)) {
                    now_pos = 5;
                    pager.setCurrentItem(1);
                    btn_next.setText("활성화하기");
                    break;
                } else {
                    now_pos = 1;
                }

            case 1:   // 1 -> 2로

                if (!hasWindowOverlayPermission(getApplicationContext())) {
                    now_pos = 6;
                    pager.setCurrentItem(2);
                    btn_next.setText("활성화하기");
                    break;
                } else {
                    now_pos = 2;
                }

            case 2:   // 2 -> 3로
                now_pos = 3;
                pager.setCurrentItem(3);
                btn_next.setText("시작하기");
                break;
            case 3:
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                finish();
                break;
            case 5:
                // 기기관리자

                //adminComponent = new ComponentName(getApplicationContext(), DeviceAdmin_Receiver.class);
                //devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

                Intent i1 = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                i1.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,adminComponent);
                i1.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"앱을 사용하기 위해선 기기 관리자 권한이 활성화되어야 합니다.");
                startActivityForResult(i1 , 5);

                // 권한 해제
                // devicePolicyManager.removeActiveAdmin(adminComponent);

                break;
            case 6:
                // overlay

                if (!hasWindowOverlayPermission(getApplicationContext())) {
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    Intent i2 = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri);
                    startActivityForResult(i2 , 6);
                } else {
                    now_pos = 2;
                    btn_next_clicked(null);
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 5:
                if (devicePolicyManager.isAdminActive(adminComponent)) {
                    now_pos = 1;
                    btn_next_clicked(null);
                }
                break;
            case 6:
                if (hasWindowOverlayPermission(getApplicationContext())) {
                    now_pos = 2;
                    btn_next_clicked(null);
                }
                break;
        }



        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        btn_next = (Button) findViewById(R.id.btn_next);

        pager = (ViewPager) findViewById(R.id.view_pager);
        adapter = new WelcomeAdapter(getSupportFragmentManager());
        pager.setAdapter(adapter);

        indicator = (DotIndicator) findViewById(R.id.indicator);
        indicator.setSelectedDotColor(Color.parseColor("#FFFFFF"));
        indicator.setSelectedDotDiameterDp(10);
        indicator.setUnselectedDotColor(Color.parseColor("#595959"));
        indicator.setNumberOfItems(adapter.getCount());

        pager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
                //return false;
            }
        });

        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                indicator.setSelectedItem(pager.getCurrentItem(), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
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
