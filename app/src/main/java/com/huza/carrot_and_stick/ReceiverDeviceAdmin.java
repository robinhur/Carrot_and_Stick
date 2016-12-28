package com.huza.carrot_and_stick;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by HuZA on 2016-11-11.
 */

public class ReceiverDeviceAdmin extends DeviceAdminReceiver {

    private static final String PACKAGE_NAME = "Carrot_and_Stick";

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.i(PACKAGE_NAME, "ReceiverDeviceAdmin : onEnabled");
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.i(PACKAGE_NAME, "ReceiverDeviceAdmin : onDisabled");
        super.onDisabled(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        Log.i(PACKAGE_NAME, "ReceiverDeviceAdmin : onDisableRequested");
        return "기기관리자 권한 해제를 요청합니다.";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(PACKAGE_NAME, "ReceiverDeviceAdmin : onReceive : " + intent.getAction());
        super.onReceive(context, intent);
    }
}
