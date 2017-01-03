package com.huza.carrot_and_stick;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HuZA on 2016-11-14.
 */

public class ServiceCreditTicker extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    NotificationManager nm;
    PendingIntent pendingIntent;
    int user_credit;
    int second;

    int what;
    String extra_data;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public ServiceCreditTicker() {
        Log.d(PACKAGE_NAME, "ServiceCreditTicker 생성");
        user_credit = -1;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        what = 0;
        extra_data = null;
        bindService(new Intent(getApplicationContext(), ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
        what = 501;
        sendMessage();
    }
    @Override
    public void onDestroy() {
        Log.d(PACKAGE_NAME, "ServiceCreditTicker 소멸");

        timer.cancel();
        timerTask = null;

        Log.d(PACKAGE_NAME, "ServiceCreditTicker : onDestroy : notification을 삭제합니다");
        //// noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////

        super.onDestroy();
    }

    public void requestTOdisconnect() {
        what = 597;
        sendMessage();
    }
    public void close_CT_service() {
        what = 599;
        extra_data = String.valueOf(second);
        sendMessage();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(PACKAGE_NAME, "ServiceCreditTicker : onStartCommand 생성");

        if (intent == null) {
            Log.d(PACKAGE_NAME, "ServiceCreditTicker : intent가 없으므로 gg");
            stopSelf();
        }

        if (intent.getAction() != null) {
            Log.d(PACKAGE_NAME, "ServiceCreditTicker : " + intent.getAction());
            switch (intent.getAction()) {
                case "CLICKtoCLOSE":
                    requestTOdisconnect();
                    break;
                default:
                    break;
            }

            return super.onStartCommand(intent, flags, startId);
        }

        pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        editor = pref.edit();

        user_credit = intent.getIntExtra("user_credit", -1);
        Log.d(PACKAGE_NAME, "ServiceCreditTicker : Received credit " + user_credit);

        //// 혹시 몰라 noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////
        Intent i = new Intent(this, ServiceCreditTicker.class);
        i.setAction("CLICKtoCLOSE");
        i.setFlags(START_FLAG_REDELIVERY);
        pendingIntent = PendingIntent.getService(this, 0, i, PendingIntent.FLAG_UPDATE_CURRENT);
        ///////////////////////////////
        setNotification();
        ///////////////////////////////

        second = 0;
        count_credit();

        return super.onStartCommand(intent, flags, startId);
    }

    public void setNotification() {
        Notification.Builder mBuilder = new Notification.Builder(getApplicationContext());

        mBuilder.setContentTitle("현재 당근을 사용 중입니다")
                .setContentText("사용 시간 :\t"+(second/60)+"분 "+(second%60)+"초 (차감 Credit : " + second + ")")
                .setTicker("당근을 사용합니다!!")
                .setSmallIcon(R.drawable.carrot_noti)
                .setContentIntent(pendingIntent)
                .setWhen(System.currentTimeMillis())
                .setStyle(new Notification.BigTextStyle()
                    .bigText("사용 시간 :\t"+(second/60)+"분 "+(second%60)+"초 (차감 Credit : " + second + ")")
                    .setSummaryText("내 Credit : " + user_credit)
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder.setCategory(Notification.CATEGORY_MESSAGE)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        Notification noti = mBuilder.build();
        noti.flags = Notification.FLAG_NO_CLEAR;

        nm.notify(737, noti);
    }

    Timer timer = new Timer();
    TimerTask timerTask;

    void count_credit() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (user_credit - second <= 0){
                    Log.d(PACKAGE_NAME, "ServiceCreditTicker : TimerTask : 사용시간 초과!!!");
                    //stopSelf();
                    return;
                }

                second++;

                editor.putInt("second" , second);
                editor.commit();

                setNotification();
            }
        };

        timer.schedule(timerTask, 0 , 1000);
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


    ////////////////////////////////////////////////////////////////////////////////////ver.161228//
    /////        ////          ////   //////  ////       ///////////// Background Service///////////
    ///  ////////////  ////////////  /  ////  ////  ////  /////////// CT connected       : 501 /////
    ////        /////          ////  ///  //  ////  /////  ////////// CT diconn req      : 597 /////
    //////////   ////  ////////////  /////    ////  ////  /////////// CT end msg         : 599 /////
    ///        //////          ////  ///////  ////       ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    private void sendMessage() {
        Log.d(PACKAGE_NAME, "ServiceCreditTicker : MESSAGE : sendMessage = " + mBound_background + " : " + what);

        if (!mBound_background)
            bindService(new Intent(this, ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
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

            if (what == 599) {
                unbindService(mConnection_background);
                mConnection_background.onServiceDisconnected(null);

                stopSelf();
            }

            what = 0;
            extra_data = null;
        }
    }


    ////////////////////////ver.161228//
    ///// Connect w/CT       : 500 /////
    ///// send Credit        : 502 /////
    ///// alert OutgoingCall : 552 /////
    ///// Disconnect w/CT    : 598 /////
    ////////////////////////////////////
    @Override
    public IBinder onBind(Intent intent) {
        return TickerMessenger.getBinder();
    }

    final Messenger TickerMessenger = new Messenger(new TickerIncomingHandler());

    class TickerIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(PACKAGE_NAME, "ServiceCreditTicker : MESSAGE : BackgroundIncomingHandler = " + msg.what);

            switch (msg.what) {
                case 500:
                    Log.d(PACKAGE_NAME, "ServiceCreditTicker : MESSAGE : BackgroundIncomingHandler : Background connected");
                    break;
                case 502:
                    Log.d(PACKAGE_NAME, "ServiceCreditTicker : MESSAGE : BackgroundIncomingHandler : Credit received");
                    break;
                case 552:
                    Log.d(PACKAGE_NAME, "ServiceCreditTicker : MESSAGE : BackgroundIncomingHandler : OUTGOING!!");
                    requestTOdisconnect();
                    break;
                case 598:
                    close_CT_service();
                    break;
            }

        }
    }
}
