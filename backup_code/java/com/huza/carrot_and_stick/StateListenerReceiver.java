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

import static android.content.Context.MODE_PRIVATE;

public class StateListenerReceiver extends BroadcastReceiver {

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.AlwaysOnTop";
    final String Background_SERVICE_NAME = "com.huza.carrot_and_stick.BackgroundService";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.CreditTickerService";

    Context context;

    ActivityManager serviceChecker;

    boolean aot_started;

    public StateListenerReceiver() {
    }
    public StateListenerReceiver(Context mcontext) {
        Log.d(PACKAGE_NAME, "StateListenerReceiver : StateListenerReceiver 생성");

        aot_started = true;
        context = mcontext;
        checkAoTServiceRunning(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Log.d(PACKAGE_NAME, "StateListenerReceiver : aot_started : " + aot_started);

        switch (intent.getAction()) {

            case Intent.ACTION_BOOT_COMPLETED:
            case "com.huza.carrot_and_stick.restartBACKGROUNDSERVICE":
                Log.d(PACKAGE_NAME, "StateListenerReceiver : ACTION_BOOT_COMPLETED");
                SharedPreferences pref = context.getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
                Log.d(PACKAGE_NAME, "StateListenerReceiver : 로그인 : "+ pref.getBoolean("isLoggedin", false));
                if (pref.getBoolean("isLoggedin", false)) {
                    checkBackgroundServiceRunning(context);
                }
                break;

            case Intent.ACTION_USER_PRESENT:
            case "com.huza.carrot_and_stick.restartAoTSERVICE":
                Log.d(PACKAGE_NAME, "StateListenerReceiver : ACTION_USER_PRESENT");
                checkAoTServiceRunning(context);
                aot_started = true;
                break;

            case Intent.ACTION_SCREEN_OFF:
                Log.d(PACKAGE_NAME, "StateListenerReceiver : ACTION_SCREEN_OFF");
                checkCreditTickerServiceRunning(context);
                break;

        }
    }

    Messenger mService_CreditTicker = null;
    boolean mBound_CreditTicker;
    private ServiceConnection mConnection_CreditTicker = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_CreditTicker = new Messenger(iBinder);
            mBound_CreditTicker = true;
            checkCreditTickerServiceRunning(context);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService_CreditTicker = null;
            mBound_CreditTicker = false;
        }
    };

    public void checkCreditTickerServiceRunning(Context context) {
        Log.d(PACKAGE_NAME, "StateListenerReceiver : checkCreditTickerServiceRunning");

        serviceChecker = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (CreditTicker_SERVICE_NAME.equals(runningServiceInfo.service.getClassName())) {
                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkCreditTickerServiceRunning service 찾음 : " + mBound_CreditTicker);

                if (!mBound_CreditTicker) {
                    context.bindService(new Intent(context, CreditTickerService.class), mConnection_CreditTicker, Context.BIND_AUTO_CREATE);
                    return;
                }

                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkCreditTickerServiceRunning Message 날린다?");
                Message msg = Message.obtain(null, 1, 0, 0);
                try {
                    mService_CreditTicker.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                context.unbindService(mConnection_CreditTicker);
                mConnection_CreditTicker.onServiceDisconnected(null);
                return;
            }
        }
    }

    Messenger mService_AoT = null;
    boolean mBound_AoT;
    private ServiceConnection mConnection_AoT = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_AoT = new Messenger(iBinder);
            mBound_AoT = true;
            checkAoTServiceRunning(context);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService_AoT = null;
            mBound_AoT = false;
        }
    };

    public void checkAoTServiceRunning(Context context) {
        Log.d(PACKAGE_NAME, "StateListenerReceiver : checkAoTServiceRunning");

        serviceChecker = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (AoT_SERVICE_NAME.equals(runningServiceInfo.service.getClassName())) {
                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkAoTServiceRunning service 찾음 : " + "aot_started : " + aot_started + "  mBound_AoT : " + mBound_AoT);

                if (aot_started) {
                    if (!mBound_AoT) {
                        context.bindService(new Intent(context, AlwaysOnTop.class), mConnection_AoT, Context.BIND_AUTO_CREATE);
                        return;
                    }

                    Log.d(PACKAGE_NAME, "StateListenerReceiver : checkAoTServiceRunning Message 날린다?");
                    Message msg = Message.obtain(null, 1, 0, 0);
                    try {
                        mService_AoT.send(msg);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    context.unbindService(mConnection_AoT);
                    mConnection_AoT.onServiceDisconnected(null);
                }

                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkAoTServiceRunning service 찾음 : " + "aot_started : " + aot_started);

                return;
            }
        }

        Log.d(PACKAGE_NAME, "StateListenerReceiver checkAoTServiceRunning service 못찾아서 AlwaysOnTop 시작");
        context.startService(new Intent(context, AlwaysOnTop.class));

    }

    public void checkBackgroundServiceRunning(Context context) {
        Log.d(PACKAGE_NAME, "StateListenerReceiver : checkBackgroundServiceRunning");

        serviceChecker = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (Background_SERVICE_NAME.equals(runningServiceInfo.service.getClassName())) {
                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkBackgroundServiceRunning service 찾음");
                return;
            }
        }

        Log.d(PACKAGE_NAME, "StateListenerReceiver checkBackgroundServiceRunning service 못찾아서 Background Service 시작");
        context.startService(new Intent(context, BackgroundService.class));

    }


}