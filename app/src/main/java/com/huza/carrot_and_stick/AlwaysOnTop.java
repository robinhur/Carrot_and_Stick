package com.huza.carrot_and_stick;

import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

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

    AOTAdapter aot_adapter;
    ViewPager aot_pager;
    DotIndicator aot_indicator;

    TextView tv_time;
    TextView tv_credit;
    TextView tv_main_credit;
    WindowManager w_manager;
    final int ui_Options =
            //View.SYSTEM_UI_FLAG_FULLSCREEN
            //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            //View.SYSTEM_UI_FLAG_LOW_PROFILE
            //| View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
    //// TYPE_SYSTEM_ERROR or TYPE_PRIORITY_PHONE or TYPE_PHONE
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

        aot_pager = (ViewPager) OnTop_view.findViewById(R.id.AOT_viewpager);
        aot_adapter = new AOTAdapter(getBaseContext());
        aot_pager.setClipToPadding(false);
        aot_pager.setPadding(100,15,100,0);
        aot_pager.setPageMargin(50);
        aot_pager.setAdapter(aot_adapter);
        aot_pager.setCurrentItem(1);

        aot_indicator = (DotIndicator) OnTop_view.findViewById(R.id.AOT_indicator);
        aot_indicator.setSelectedDotColor(Color.parseColor("#FFFFFF"));
        aot_indicator.setSelectedDotDiameterDp(10);
        aot_indicator.setUnselectedDotColor(Color.parseColor("#888888"));
        aot_indicator.setNumberOfItems(aot_adapter.getCount());
        aot_indicator.setSelectedItem(1, false);

        aot_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                aot_indicator.setSelectedItem(aot_pager.getCurrentItem(), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tv_time = (TextView) OnTop_view.findViewById(R.id.tv_time);
        tv_main_credit = (TextView) OnTop_view.findViewById(R.id.tv_main_credit);

        OnTop_view.setSystemUiVisibility(ui_Options);

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
                tv_main_credit.setText(dataSnapshot.getValue().toString());
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
                    aot_indicator.setSelectedItem(1, true);
                    aot_pager.setCurrentItem(1);
                    tv_credit.setText(dataSnapshot.getValue().toString());
                    tv_main_credit.setText(dataSnapshot.getValue().toString());
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

        //// indicator listener 장착 ////
        aot_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                aot_indicator.setSelectedItem(aot_pager.getCurrentItem(), true);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        ////////////////////////////////

        //// OnTop_view setOnSystemUiVisibilityChangeListener 장착 ////
        OnTop_view.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int i) {
                Log.d(PACKAGE_NAME, "OnTop_view : 비정상 setOnSystemUiVisibilityChangeListener : before" + i);
                OnTop_view.setSystemUiVisibility(ui_Options);
                Log.d(PACKAGE_NAME, "OnTop_view : 비정상 setOnSystemUiVisibilityChangeListener : after" + i);
            }
        });
        ////////////////////////////////

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
        //Button btn_finalclose = (Button) OnTop_view.findViewById(R.id.button2);
        //btn_finalclose.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {
        //        btn_finalclose_clicked(view);
        //    }
        //});
        ////////////////////////////////
    }

    public void btn_finalclose_clicked(View v) {
        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("isLoggedin", false);
        editor.commit();
        Log.d(PACKAGE_NAME, "AlwaysOnTop : 로그아웃 완료!! : " + pref.getBoolean("isLoggedin", false));

        Log.d(PACKAGE_NAME, "AlwaysOnTop : AoT screen setSystemUiVisibility 해제");
        OnTop_view.setOnSystemUiVisibilityChangeListener(null);
        OnTop_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

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

        Log.d(PACKAGE_NAME, "AlwaysOnTop : onStartCommand");
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

    public class AOTAdapter extends PagerAdapter {

        final String PACKAGE_NAME = "Carrot_and_Stick";
        int[] aot_screen = {
                R.layout.aot_menu,
                R.layout.aot_main,
                R.layout.aot_history
        };

        Context mContext;

        public AOTAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup layout = (ViewGroup) inflater.inflate(aot_screen[position], container, false);
            container.addView(layout);


            if (position == 1){
                tv_credit = (TextView) layout.findViewById(R.id.tv_credit);
            }


            return layout;

        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return aot_screen.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
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

            Log.d(PACKAGE_NAME, "AlwaysOnTop : IncomingHandler = " + msg.what);

            //////////////////////////  code  ////////////////////////
            ////// 1 : setSystemUiVisibility & select main card //////
            //////////////////////////////////////////////////////////

            switch (msg.what) {
                case 1:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : IncomingHandler : 화면 설정!!!");
                    OnTop_view.setSystemUiVisibility(2);

                    Log.d(PACKAGE_NAME, "AlwaysOnTop : IncomingHandler : Main Card 선택!!!");
                    aot_indicator.setSelectedItem(1, true);
                    aot_pager.setCurrentItem(1);

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }
}
