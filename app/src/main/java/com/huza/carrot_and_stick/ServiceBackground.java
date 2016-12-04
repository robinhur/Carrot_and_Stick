package com.huza.carrot_and_stick;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class ServiceBackground extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.AlwaysOnTop";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.CreditTickerService";

    ReceiverStateListener statelistener = null;
    NotificationManager nm;

    public ServiceBackground() {
        Log.d(PACKAGE_NAME, "ServiceBackground 생성");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // init service!!!!! //
        if (statelistener == null) {
            statelistener = new ReceiverStateListener(this);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
            intentFilter.setPriority(2147483647);

            registerReceiver(statelistener, intentFilter);

            Log.d(PACKAGE_NAME, "ServiceBackground : init : ReceiverStateListener 등록");
        }

        Start_AoT();
    }

    public void Start_AoT() {
        if (checkServiceRunning(AoT_SERVICE_NAME)) return;

        //// 혹시 몰라 noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////

        Log.d(PACKAGE_NAME, "ServiceBackground : Start_AoT : AoT 서비스를 시작합니다");
        startService(new Intent(this, ServiceAlwaysOnTop.class));
    }
    public void Close_CreditTicker() {
        if (checkServiceRunning(CreditTicker_SERVICE_NAME)){
            Log.d(PACKAGE_NAME, "ServiceBackground : Close_CreditTicker : CreditTicker 서비스를 정지합니다");
            stopService(new Intent(this, ServiceCreditTicker.class));
        }

        //// 혹시 몰라 noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////
    }

    public boolean checkServiceRunning(String service_name) {

        ActivityManager serviceChecker = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (service_name.equals(runningServiceInfo.service.getClassName())){
                Log.d(PACKAGE_NAME, "ServiceBackground : checkServiceRunning : "+ service_name + " = found!!!!");
                return true;
            }
        }

        Log.d(PACKAGE_NAME, "ServiceBackground : checkServiceRunning : "+ service_name + " = not running");
        return false;

    }

    @Override
    public IBinder onBind(Intent intent) {
        return BackgroundMessenger.getBinder();
    }
    final Messenger BackgroundMessenger = new Messenger(new BackgroundIncomingHandler());
    class BackgroundIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(PACKAGE_NAME, "ServiceBackground : BackgroundIncomingHandler = " + msg.what);

            ///////////////////////////////////
            ///// AoT gogogogo       : 1  /////
            ///// CreditTicker close : 2  /////
            ///// NEW OUTGOING CALL  : 3  /////
            ///// Finally Close      : 99 /////
            ///////////////////////////////////

            switch (msg.what) {
                case 1:
                    Start_AoT();
                    break;
                case 2:
                    Close_CreditTicker();
                    break;
                case 3:
                    Log.d(PACKAGE_NAME, "ServiceBackground : BackgroundIncomingHandler : 3 : " + msg.getData().getString("extra_data"));
                    break;
                case 99:
                    break;
            }

        }
    }
}
