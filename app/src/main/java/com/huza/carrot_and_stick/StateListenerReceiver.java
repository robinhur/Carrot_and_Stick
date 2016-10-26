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

    public StateListenerReceiver() {
    }
    public StateListenerReceiver(Context mcontext) {
        Log.d(PACKAGE_NAME, "StateListenerReceiver : StateListenerReceiver 생성");

        context = mcontext;
        checkAoTServiceRunning(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.

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
                break;

            case Intent.ACTION_SCREEN_OFF:
                Log.d(PACKAGE_NAME, "StateListenerReceiver : ACTION_SCREEN_OFF");
                checkCreditTickerServiceRunning(context);
                break;

        }
    }

    Messenger mService = null;
    boolean mBound;
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService = new Messenger(iBinder);
            mBound = true;
            checkCreditTickerServiceRunning(context);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService = null;
            mBound = false;
        }
    };

    public void checkCreditTickerServiceRunning(Context context) {

        serviceChecker = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (CreditTicker_SERVICE_NAME.equals(runningServiceInfo.service.getClassName())) {
                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkCreditTickerServiceRunning service 찾음 : " + mBound);

                if (!mBound) {
                    context.bindService(new Intent(context, CreditTickerService.class), mConnection, Context.BIND_AUTO_CREATE);
                    return ;
                }

                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkCreditTickerServiceRunning Message 날린다?");
                Message msg = Message.obtain(null, 1, 0, 0);
                try {
                    mService.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                context.unbindService(mConnection);
                mConnection.onServiceDisconnected(null);
            }
        }

    }

    public void checkAoTServiceRunning(Context context) {

        serviceChecker = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (AoT_SERVICE_NAME.equals(runningServiceInfo.service.getClassName())) {
                Log.d(PACKAGE_NAME, "StateListenerReceiver : checkAoTServiceRunning service 찾음");
                return;
            }
        }

        Log.d(PACKAGE_NAME, "StateListenerReceiver checkAoTServiceRunning service 못찾아서 AlwaysOnTop 시작");
        context.startService(new Intent(context, AlwaysOnTop.class));

    }

    public void checkBackgroundServiceRunning(Context context) {

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