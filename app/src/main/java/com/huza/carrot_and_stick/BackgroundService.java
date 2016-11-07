package com.huza.carrot_and_stick;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

public class BackgroundService extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    BroadcastReceiver statelistener;
    UserData now_user;

    public BackgroundService() {
        Log.d(PACKAGE_NAME, "BackgroundService : BackgroundService 생성");
    }

    @Override
    public void onCreate() {
        Log.d(PACKAGE_NAME, "BackgroundService : onCreate 호출");

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isFirst", true);
        editor.commit();

        if (statelistener == null) {

            statelistener = new StateListenerReceiver(this);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
            //intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.setPriority(2147483647);

            registerReceiver(statelistener, intentFilter);

            Log.d(PACKAGE_NAME, "BackgroundService : StateListenerReceiver 등록");

        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(PACKAGE_NAME, "BackgroundService : CreditTicker 소멸 요청");
        stopService(new Intent(this, CreditTickerService.class));
        unregisterReceiver(statelistener);
        Log.d(PACKAGE_NAME, "BackgroundService : StateListenerReceiver 소멸 후 onDestroy");

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove("isFirst");
        editor.commit();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
