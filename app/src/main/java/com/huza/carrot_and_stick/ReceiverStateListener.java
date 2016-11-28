package com.huza.carrot_and_stick;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by HuZA on 2016-11-11.
 */

public class ReceiverStateListener extends BroadcastReceiver {

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String Main_ACTIVITY_NAME = "com.huza.carrot_and_stick.ActivityMain";
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.AlwaysOnTop";
    final String Background_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceBackground";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.CreditTickerService";

    Context mContext;
    int what;

    public ReceiverStateListener() {super();}
    public ReceiverStateListener(Context context) {
        mContext = context;
    }

    Messenger mService_background = null;
    boolean mBound_background;
    private ServiceConnection mConnection_background = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_background = new Messenger(iBinder);
            mBound_background = true;
            sendMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService_background = null;
            mBound_background = false;
        }
    };

    private void sendMessage() {
        Log.d(PACKAGE_NAME, "ReceiverStateListener : sendMessage = " + mBound_background);

        if (!mBound_background)
            mContext.bindService(new Intent(mContext, ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
        else {
            if (what == 0) return;

            Message msg = Message.obtain(null, what, 0, 0);
            try {
                mService_background.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (what == 3) {
                mContext.unbindService(mConnection_background);
                mConnection_background.onServiceDisconnected(null);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(PACKAGE_NAME, "ReceiverStateListener : onReceive = " + intent.getAction());
        mContext = context;
        what = 0;

        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case "com.huza.carrot_and_stick.restartBACKGROUNDSERVICE":
                if (isLoggedin()) {
                    if (!checkServiceRunning(Background_SERVICE_NAME))
                        context.startService(new Intent(mContext, ServiceBackground.class));
                } else {
                    if (!checkServiceRunning(Main_ACTIVITY_NAME))
                        context.startActivity(new Intent(mContext, ActivityMain.class));
                }
                break;

            ///// AoT gogogogo : 1 /////
            case Intent.ACTION_SCREEN_ON:
                Log.d(PACKAGE_NAME, "ReceiverStateListener : onReceive : SCREEN_ON is now Under Construction");
            case Intent.ACTION_USER_PRESENT:
                what = 1;
                sendMessage();
                break;

            ///// CreditTicker close : 2 /////
            case Intent.ACTION_SCREEN_OFF:
            case Intent.ACTION_SHUTDOWN:
                what = 2;
                sendMessage();
                break;

            ///// Finally Close : 3 /////
            case "com.huza.carrot_and_stick.finally_close":
                what = 3;
                sendMessage();
                break;
        }
    }

    public boolean isLoggedin() {
        SharedPreferences pref = mContext.getSharedPreferences("Carrot_and_Stick", Context.MODE_PRIVATE);
        Log.d(PACKAGE_NAME, "ReceiverStateListener : isLoggedin : "+ pref.getBoolean("isLoggedin", false));

        if (pref.getBoolean("isLoggedin", false)) {
            return true;
        } else {
            return false;
        }
    }
    public boolean checkServiceRunning(String service_name) {

        ActivityManager serviceChecker = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (service_name.equals(runningServiceInfo.service.getClassName())){
                Log.d(PACKAGE_NAME, "ReceiverStateListener : checkServiceRunning : "+ service_name + " = found!!!!");
                return true;
            }
        }

        Log.d(PACKAGE_NAME, "ReceiverStateListener : checkServiceRunning : "+ service_name + " = not running");
        return false;

    }
}
