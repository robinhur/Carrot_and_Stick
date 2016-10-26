package com.huza.carrot_and_stick;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class CreditTickerService extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String BACKGROUND_SERVICE = "com.huza.carrot_and_stick.BackgroundService";

    NotificationManager nm;
    PendingIntent pendingIntent;
    int Credit;
    int second;

    Timer timer = new Timer();
    TimerTask timerTask;
    Handler handler = new Handler();

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    public CreditTickerService() {
        Log.d(PACKAGE_NAME, "CreditTickerService : CreditTicker 생성");
    }

    @Override
    public void onDestroy() {

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);

        //////// 정산 /////////
        if (!(pref.getString("startTIME", "none").equals("none"))) {

            //////// 정산 report /////////
            Calendar now_time = Calendar.getInstance();

            Log.d(PACKAGE_NAME, "CreditTickerService : 정산 : 사용시작 : "+ pref.getString("startTIME", "none").toString());
            Log.d(PACKAGE_NAME, "CreditTickerService : 정산 : 사용끝 : "+ now_time.getTime().toString());
            Log.d(PACKAGE_NAME, "CreditTickerService : 정산 : 사용시간 : " + second);

            SharedPreferences.Editor editor = pref.edit();
            editor.remove("startTIME");
            editor.remove("second");
            editor.commit();
            //////////////////////////////

            ///////// Credit 차감 //////////
            databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").setValue(Credit-second);
            ////////////////////////////////

        }

        Log.d(PACKAGE_NAME, "CreditTickerService : CreditTicker 소멸");
        timer.cancel();
        nm = null;
        super.onDestroy();
        try {
            pendingIntent.send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(PACKAGE_NAME, "CreditTickerService : onStartCommand 생성");

        if (intent != null) {
            firebaseDatabase = FirebaseDatabase.getInstance();
            databaseReference = firebaseDatabase.getReference();

            Credit = intent.getIntExtra("Credit", -1);
            Log.d(PACKAGE_NAME, "CreditTickerService : Received credit " + Credit);

            nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.cancel(737);

            pendingIntent = PendingIntent.getService(this, 0, new Intent(this, AlwaysOnTop.class), PendingIntent.FLAG_UPDATE_CURRENT);

            Notification.Builder mBuilder = new Notification.Builder(this);
            mBuilder.setSmallIcon(R.drawable.carrot_noti);
            mBuilder.setWhen(System.currentTimeMillis() + 500);
            mBuilder.setContentIntent(pendingIntent);
            mBuilder.setPriority(Notification.PRIORITY_HIGH);

            Notification.BigTextStyle style = new Notification.BigTextStyle(mBuilder);
            style.setSummaryText("내 Credit : " + Credit);
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
            count_credit();
        } else {
            Log.d(PACKAGE_NAME, "CreditTickerService : intent가 없어서 gg");
            stopSelf();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    void count_credit() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (Credit - second <= 0){
                    Log.d(PACKAGE_NAME, "CreditTickerService : TimerTask : 사용시간 초과!!!");
                    stopSelf();
                    return;
                }

                second++;

                SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
                SharedPreferences.Editor editor = pref.edit();
                editor.putInt("second" , second);
                editor.commit();

                Notification.Builder mBuilder = new Notification.Builder(getApplicationContext());
                mBuilder.setSmallIcon(R.drawable.carrot_noti);
                mBuilder.setWhen(System.currentTimeMillis() + 500);
                mBuilder.setContentIntent(pendingIntent);
                mBuilder.setPriority(Notification.PRIORITY_HIGH);

                Notification.BigTextStyle style = new Notification.BigTextStyle(mBuilder);
                style.setSummaryText("내 Credit : " + Credit);
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
            }
        };

        timer.schedule(timerTask, 0 , 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mMessenger.getBinder();
    }

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {

            Log.d(PACKAGE_NAME, "CreditTickerService : IncomingHandler = " + msg.what);

            ////////////////  code  ////////////////
            ////// 1 : delete noti & stopSelf //////
            ////////////////////////////////////////

            switch (msg.what) {
                case 1:;
                    Log.d(PACKAGE_NAME, "CreditTickerService : IncomingHandler : 노티 지우기!!!");
                    nm.cancel(737);

                    Log.d(PACKAGE_NAME, "CreditTickerService : stopSelf()");
                    stopSelf();
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}