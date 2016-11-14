package com.huza.carrot_and_stick;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by HuZA on 2016-11-14.
 */

public class ServiceAlwaysOnTop extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.AlwaysOnTop";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.CreditTickerService";

    public ServiceAlwaysOnTop() {
        Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop 생성");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
