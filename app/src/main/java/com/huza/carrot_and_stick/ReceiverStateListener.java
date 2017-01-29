package com.huza.carrot_and_stick;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
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
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceAlwaysOnTop";
    final String Background_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceBackground";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceCreditTicker";

    Context mContext;
    int what;
    String extra_data;

    public ReceiverStateListener() {super();}
    public ReceiverStateListener(Context context) {
        mContext = context;
    }

    ////////////////////////////////////////////////////////////////////////////////////ver.161228//
    /////        ////          ////   //////  ////       //////////// Background ///////////////////
    ///  ////////////  ////////////  /  ////  ////  ////  ////////// AoT gogogogo       : 1   //////
    ////        /////          ////  ///  //  ////  /////  ///////// NEW OUTGOING CALL  : 2   //////
    //////////   ////  ////////////  /////    ////  ////  ////////// CreditTicker close : 5   //////
    ///        //////          ////  ///////  ////       /////////// Finally Close      : 99  //////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void sendMessage() {
        Log.d(PACKAGE_NAME, "ReceiverStateListener : MESSAGE : mContext = " + mContext);
        Log.d(PACKAGE_NAME, "ReceiverStateListener : MESSAGE : sendMessage = " + mBound_background + " : " + what);

        if (!mBound_background)
            mContext.getApplicationContext().bindService(new Intent(mContext.getApplicationContext(), ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
        else {
            if (what == 0) return;

            Message msg = Message.obtain(null, what, 0, 0);

            if (extra_data != null) {
                Bundle data = new Bundle();
                data.putString("extra_data" , extra_data);
                msg.setData(data);
            }

            try {
                mService_background.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (what == 99) {
                mContext.unbindService(mConnection_background);
                mConnection_background.onServiceDisconnected(null);
            }

            what = 0;
            extra_data = null;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(PACKAGE_NAME, "ReceiverStateListener : onReceive : MESSAGE : " + intent.getAction());
        mContext = context;
        what = 0;
        extra_data = null;

        switch (intent.getAction()) {
            case Intent.ACTION_BOOT_COMPLETED:
            case "com.huza.carrot_and_stick.restartBACKGROUNDSERVICE":
                if (isLoggedin()) {
                    if (!checkServiceRunning(Background_SERVICE_NAME)){
                        mContext.startService(new Intent(mContext, ServiceBackground.class));
                        //mContext.bindService(new Intent(mContext, ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
                    } else {
                        mContext.sendBroadcast(new Intent("com.huza.carrot_and_stick.restartAoTSERVICE"));
                    }
                } else {
                    if (!checkServiceRunning(Main_ACTIVITY_NAME))
                        mContext.startActivity(new Intent(mContext, ActivityMain.class));
                }
                break;

            ///// AoT gogogogo : 1 /////
            case Intent.ACTION_SCREEN_ON:
                Log.d(PACKAGE_NAME, "ReceiverStateListener : onReceive : SCREEN_ON is now Under Construction");
                break;
            case Intent.ACTION_USER_PRESENT:
            case "com.huza.carrot_and_stick.restartAoTSERVICE":
                what = 1;
                sendMessage();
                break;

            ///// to AoT, NEW OUTGOING CALL : 2 /////
            case Intent.ACTION_NEW_OUTGOING_CALL:
                what = 2;
                extra_data = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
                sendMessage();
                break;

            ///// CreditTicker close : 5 /////
            case Intent.ACTION_SCREEN_OFF:
                Log.d(PACKAGE_NAME, "ReceiverStateListener : onReceive : SCREEN_OFF is now Under Construction");
            case Intent.ACTION_SHUTDOWN:
                what = 5;
                sendMessage();
                break;

            ///// Finally Close : 99 /////
            case "com.huza.carrot_and_stick.finally_close":
                what = 99;
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


    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    Messenger mService_background = null;
    boolean mBound_background;
    private ServiceConnection mConnection_background = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(PACKAGE_NAME, "ReceiverStateListener : onServiceConnected");
            mService_background = new Messenger(iBinder);
            mBound_background = true;
            sendMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(PACKAGE_NAME, "ReceiverStateListener : onServiceDisconnected");
            mService_background = null;
            mBound_background = false;
        }
    };
}
