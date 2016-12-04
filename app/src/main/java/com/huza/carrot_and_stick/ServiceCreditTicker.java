package com.huza.carrot_and_stick;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

/**
 * Created by HuZA on 2016-11-14.
 */

public class ServiceCreditTicker extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    NotificationManager nm;
    PendingIntent pendingIntent;
    int user_credit;
    int second;

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

    }

    public void letscloseticker() {
        what = 599;
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
                    letscloseticker();
                    break;
                default:
                    break;
            }

            return super.onStartCommand(intent, flags, startId);
        }

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

        //mBuilder.addAction(R.drawable.calendar_v, "Yes", pendingIntentYes);

        Notification.Builder mBuilder = new Notification.Builder(this);
        mBuilder.setSmallIcon(R.drawable.carrot_noti);
        mBuilder.setWhen(System.currentTimeMillis() + 500);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setPriority(Notification.PRIORITY_HIGH);

        Notification.BigTextStyle style = new Notification.BigTextStyle(mBuilder);
        style.setSummaryText("내 Credit : " + user_credit);
        style.bigText(
                "사용 시간 :\t"+(second/60)+"분 "+(second%60)+"초 (차감 Credit : " + second + ")"
        );
        mBuilder.setStyle(style);
        //mBuilder.setTicker("안되지만 여기는 ticker!!!");

        mBuilder.setContentTitle("현재 당근을 사용 중입니다");
        mBuilder.setContentText("사용 시간 :\t"+(second/60)+"분 "+(second%60)+"초 (차감 Credit : " + second + ")");

        Notification noti = mBuilder.build();
        noti.flags = Notification.FLAG_NO_CLEAR;

        nm.notify(737, noti);

        second = 0;
        //count_credit();

        return super.onStartCommand(intent, flags, startId);
    }

    int what;
    String extra_data;

    Messenger mService_background = null;
    boolean mBound_background;
    private ServiceConnection mConnection_background = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_background = new Messenger(iBinder);
            mBound_background = true;
            what = 50;
            sendMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService_background = null;
            mBound_background = false;
        }
    };

    private void sendMessage() {
        Log.d(PACKAGE_NAME, "ServiceCreditTicker : MESSAGE : sendMessage = " + mBound_background + " : " + what);

        if (!mBound_background)
            bindService(new Intent(getApplicationContext(), ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
        else {
            if (what == 0) return;

            Message msg = Message.obtain(null, what, 0, 0);

            try {
                mService_background.send(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            if (what == 599) {
                unbindService(mConnection_background);
                mConnection_background.onServiceDisconnected(null);
            }
        }

        what = 0;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return TickerMessenger.getBinder();
    }

    final Messenger TickerMessenger = new Messenger(new TickerIncomingHandler());

    class TickerIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(PACKAGE_NAME, "ServiceCreditTicker : MESSAGE : BackgroundIncomingHandler = " + msg.what);

            /////////////////////////////////////
            ///// Connected with BG  : 500  /////
            ///// Requested to Close : 598  /////
            ///// Finally Close      : 599  /////
            /////////////////////////////////////

            switch (msg.what) {
                case 500:
                    break;
                case 598:
                    letscloseticker();
                    break;
                case 599:
                    stopSelf();
                    break;
            }

        }
    }
}
