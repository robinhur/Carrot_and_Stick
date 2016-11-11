package com.huza.carrot_and_stick;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

public class ServiceBackground extends Service {
    final String PACKAGE_NAME = "Carrot_and_Stick";
    ReceiverStateListener statelistener = null;

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
            intentFilter.setPriority(2147483647);

            registerReceiver(statelistener, intentFilter);

            Log.d(PACKAGE_NAME, "ServiceBackground : ReceiverStateListener 등록");

        }

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(PACKAGE_NAME, "ServiceBackground : IncomingHandler = " + msg.what);

            //////////////////////////////////
            ///// AoT gogogogo       : 1 /////
            ///// CreditTicker close : 2 /////
            ///// Finally Close      : 3 /////
            //////////////////////////////////

        }
    }
}
