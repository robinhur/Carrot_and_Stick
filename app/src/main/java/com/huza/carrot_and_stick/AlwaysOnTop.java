package com.huza.carrot_and_stick;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class AlwaysOnTop extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String BACKGROUND_SERVICE = "com.huza.carrot_and_stick.BackgroundService";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.CreditTickerService";

    View OnTop_view;
    TextView tv_time;
    TextView tv_credit;
    WindowManager w_manager;

    SimpleDateFormat time_format = new SimpleDateFormat("hh : mm : ss", Locale.KOREA);
    Calendar now_time;

    TimerTask timertask;
    Timer timer = new Timer();
    Handler handler = new Handler();

    NotificationManager nm;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int user_credit;

    public AlwaysOnTop() {
        Log.d(PACKAGE_NAME, "AlwaysOnTop : AoT 생성");
        user_credit = -1;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        //// 혹시 몰라 noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////

        //// AoT 생성 ////
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        OnTop_view = inflater.inflate(R.layout.service_alwaysontop, null);
        tv_time = (TextView) OnTop_view.findViewById(R.id.tv_time);
        tv_credit = (TextView) OnTop_view.findViewById(R.id.tv_credit);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);

        w_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        w_manager.addView(OnTop_view, params);
        /////////////////

        //// firebase 연결 ////
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tv_credit.setText(dataSnapshot.getValue().toString());
                user_credit = Integer.valueOf(dataSnapshot.getValue().toString());

                //// 비정산 처리 ㄱㄱ ////
                settle_up();
                //////////////////////////
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        databaseReference.child("users").child(pref.getString("user_uid", null)).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if (dataSnapshot.getKey().toString().equals("credit")) {
                    tv_credit.setText(dataSnapshot.getValue().toString());
                    user_credit = Integer.valueOf(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ///////////////////////

        //// 사용하기 버튼 ////
        Button btn_close = (Button) OnTop_view.findViewById(R.id.button);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                close_AoT_service();
            }
        });
        ///////////////////////

        //// 임시 서비스 종료 버튼 ////
        Button btn_finalclose = (Button) OnTop_view.findViewById(R.id.button2);
        btn_finalclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btn_finalclose_clicked(view);
            }
        });
        ////////////////////////////////
    }

    public void btn_finalclose_clicked(View v) {
        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedin", false);
        editor.commit();
        Log.d(PACKAGE_NAME, "AlwaysOnTop : 로그아웃 완료!! : " + pref.getBoolean("isLoggedin", false));

        Log.d(PACKAGE_NAME, "AlwaysOnTop : BackgroundService 소멸 요청");
        stopService(new Intent(this, BackgroundService.class));

        stopSelf();
    }

    public void close_AoT_service(){

        if (user_credit <= 0){
            if (user_credit == -1) Toast.makeText(this, "현재 로딩 중입니다.\n잠시만 기다려주세요", Toast.LENGTH_SHORT).show();
            if (user_credit == 0) Toast.makeText(this, "Credit이 0으로 사용할 수 없습니다.\nCredit을 쌓아보세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        Calendar now_time = Calendar.getInstance();
        editor.putString("startTIME", now_time.getTime().toString());

        Log.d(PACKAGE_NAME, "AlwaysOnTop : 사용시작 : "+now_time.getTime().toString());

        editor.commit();

        //////

        Log.d(PACKAGE_NAME, "AlwaysOnTop : CreditTickerService 생성");
        Intent i = new Intent(AlwaysOnTop.this, CreditTickerService.class);
        i.putExtra("Credit", user_credit);
        startService(i);

        //////

        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (OnTop_view != null) {
            w_manager.removeView(OnTop_view);
            OnTop_view = null;
        }

        Log.d(PACKAGE_NAME, "AlwaysOnTop : AoT 소멸");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        timer_start();
        return super.onStartCommand(intent, flags, startId);

    }

    private void timer_start() {

        timertask = new TimerTask() {
            @Override
            public void run() {
                textview_update();
            }
        };

        timer.schedule(timertask, 0, 1000);

    }

    private void textview_update() {
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                now_time = Calendar.getInstance();
                tv_time.setText(time_format.format(now_time.getTime()));
            }
        };

        handler.post(updater);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public void settle_up() {

        ActivityManager serviceChecker = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (CreditTicker_SERVICE_NAME.equals(runningServiceInfo.service.getClassName())) {
                Log.d(PACKAGE_NAME, "AlwaysOnTop : CreditTickerService 찾음");
                stopService(new Intent(this, CreditTickerService.class));
                return;
            }
        }

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        if (!(pref.getString("startTIME", "none").equals("none"))) {

            Log.d(PACKAGE_NAME, "AlwaysOnTop : 비정상 정산 : 사용시작 : "+ pref.getString("startTIME", "none").toString());
            Log.d(PACKAGE_NAME, "AlwaysOnTop : 비정상 정산 : 사용시간 : " + pref.getInt("second",-1));

            ///////// Credit 차감 (비정상) //////////
            databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").setValue(user_credit-pref.getInt("second",0));
            Log.d(PACKAGE_NAME, "AlwaysOnTop : 비정상 정산 : 차감 완료");
            ////////////////////////////////


            SharedPreferences.Editor editor = pref.edit();
            editor.remove("startTIME");
            editor.remove("second");
            editor.commit();

        }
    }
}
