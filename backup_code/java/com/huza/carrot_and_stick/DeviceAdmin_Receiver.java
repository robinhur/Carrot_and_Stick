package com.huza.carrot_and_stick;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by HuZA on 2016-10-26.
 */

public class DeviceAdmin_Receiver extends DeviceAdminReceiver {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    @Override
    public void onDisabled(Context context, Intent intent) {
        Log.i(PACKAGE_NAME, "MyDevicePolicyReciever : Device Admin Disabled");
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        Log.i(PACKAGE_NAME, "MyDevicePolicyReciever : Device Admin is now enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        CharSequence disableRequestedSeq = "Requesting to disable Device Admin";
        return disableRequestedSeq;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(PACKAGE_NAME, "MyDevicePolicyReciever Received: " + intent.getAction());
        super.onReceive(context, intent);
    }
}
