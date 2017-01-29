package com.huza.carrot_and_stick;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HuZA on 2016-11-14.
 */

public class ServiceAlwaysOnTop extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    static final int PHONE_STATE_NOT_YET = -1;
    static final int PHONE_STATE_IDLE = 0;
    static final int PHONE_STATE_RINGING = 1;
    static final int PHONE_STATE_OUTGOING = 2;
    static final int PHONE_STATE_OFFHOOK = 3;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ArrayList<ArrayList<String>> message_list;

    ArrayList<BarEntry> graph_dataset_past;
    ArrayList<BarEntry> graph_dataset_today;

    View OnTop_view;

    AdapterAOT aot_adapter;
    ViewPager aot_pager;
    DotIndicator aot_indicator;

    Button btn_close;
    ProgressBar PB1; // on aot
    ProgressBar PB2; // on aot
    ProgressBar PB3; // on history

    BarChart bar_graph_past;
    BarChart bar_graph_today;
    LayoutSliding aot_custom_slidinglayout;

    ListView aot_history;
    String last_forlistview;
    AdapterHistory history_adapter;

    ListView aot_setting;
    AdapterSetting setting_adapter;

    TextView tv_time;
    TextView tv_main_credit;
    ImageView iv_main_credit;
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
    int AoT_MaintextColor;
    SimpleDateFormat time_format = new SimpleDateFormat("aa hh:mm:ss", Locale.KOREA);
    Calendar now_time;

    TimerTask timertask;
    Timer timer = new Timer();
    Handler handler = new Handler();

    int user_credit = -1;

    public ServiceAlwaysOnTop() {
        Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop 생성");
    }

    String extra_data;

    //////// 홈화면 나가기 ////////
    public void gotoHomeScreen() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
    //////////////////////////////

    @Override
    public void onCreate() {
        Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop : onCreate");
        super.onCreate();

        pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        editor = pref.edit();

        user_credit = pref.getInt("user_credit", -1);

        message_list = new ArrayList<>();

        extra_data = null;
        bindService(new Intent(this, ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
        sendMessage(101, null);

        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getApplicationContext().getTheme();
        theme.resolveAttribute(android.R.attr.textColorTertiary, typedValue, true);
        TypedArray arr = getApplicationContext().obtainStyledAttributes(typedValue.data, new int[]{android.R.attr.textColorTertiary});
        AoT_MaintextColor = arr.getColor(0, -1);
        arr.recycle();

        ///홈버튼누르기///
        gotoHomeScreen();
        //////////////////

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        OnTop_view = inflater.inflate(R.layout.service_alwaysontop, null);

        // FIREBASE CRASH REPORT //
        //FirebaseCrash.report(new Exception("My first Android non-fatal error"));
        //tv_time.setText("asdf");
        ///////////////////////////

        /// 배너 광고!! ///
        MobileAds.initialize(OnTop_view.getContext(), "ca-app-pub-7701727044020114~2802773583");
        AdView mAdView = (AdView) OnTop_view.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)  //ADD TEST DEVICE MODE
                .build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop : AoT Banner : onAdClosed");
                OnTop_view.setVisibility(View.VISIBLE);
                super.onAdClosed();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop : AoT Banner : onAdFailedToLoad : " + i);
                super.onAdFailedToLoad(i);
            }

            @Override
            public void onAdLeftApplication() {
                Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop : AoT Banner : onAdLeftApplication");
                OnTop_view.setVisibility(View.INVISIBLE);
                super.onAdLeftApplication();
            }

            @Override
            public void onAdOpened() {
                Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop : AoT Banner : onAdOpened");
                super.onAdOpened();
            }

            @Override
            public void onAdLoaded() {
                Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop : AoT Banner : onAdLoaded");
                super.onAdLoaded();
            }
        });
        ///////////////////

        aot_pager = (ViewPager) OnTop_view.findViewById(R.id.AOT_viewpager);
        aot_adapter = new AdapterAOT(getBaseContext());
        aot_pager.setClipToPadding(false);
        aot_pager.setPadding(100, 15, 100, 15);
        aot_pager.setPageMargin(50);
        aot_pager.setAdapter(aot_adapter);
        aot_pager.setCurrentItem(1);

        aot_indicator = (DotIndicator) OnTop_view.findViewById(R.id.AOT_indicator);
        aot_indicator.setSelectedDotColor(Color.parseColor("#FFFFFF"));
        aot_indicator.setSelectedDotDiameterDp(10);
        aot_indicator.setUnselectedDotColor(Color.parseColor("#888888"));
        aot_indicator.setNumberOfItems(aot_adapter.getCount());
        aot_indicator.setSelectedItem(1, false);

        //// 사용하기 버튼 ////
        btn_close = (Button) OnTop_view.findViewById(R.id.button);
        btn_close.setText("");
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_creditticker();
            }
        });
        ///////////////////////

        PB1 = (ProgressBar) OnTop_view.findViewById(R.id.progress_bar1);
        PB2 = (ProgressBar) OnTop_view.findViewById(R.id.progress_bar2);
        ////////////시계////////////
        //tv_time = (TextView) OnTop_view.findViewById(R.id.tv_time);
        ////////////////////////////
        tv_main_credit = (TextView) OnTop_view.findViewById(R.id.tv_main_credit);
        iv_main_credit = (ImageView) OnTop_view.findViewById(R.id.iv_main_updown);

        OnTop_view.setSystemUiVisibility(ui_Options);
        Log.d(PACKAGE_NAME, "AlwaysOnTop : onSystemUiVisibilityChange1 : " + ui_Options);

        w_manager = (WindowManager) getSystemService(WINDOW_SERVICE);
        w_manager.addView(OnTop_view, params);

        //// indicator listener 장착 ////
        aot_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                OnTop_view.setSystemUiVisibility(ui_Options);
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
                Log.d(PACKAGE_NAME, "AlwaysOnTop : onSystemUiVisibilityChange_listen : " + i);

                OnTop_view.setSystemUiVisibility(ui_Options);

                if (aot_custom_slidinglayout != null)
                    aot_custom_slidinglayout.calculate_location();
                //Log.d(PACKAGE_NAME, "AlwaysOnTop : onSystemUiVisibilityChange_listen : " + OnTop_view.getWindowSystemUiVisibility());
            }
        });
        ////////////////////////////////

        //// SCREEN OFF button 장착 ////
        ImageView btn_screenoff = (ImageView) OnTop_view.findViewById(R.id.btn_screenoff);
        btn_screenoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                off_screen();
            }
        });
        ////////////////////////////////

    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (OnTop_view != null) {
            w_manager.removeView(OnTop_view);
            OnTop_view = null;
        }

        //manager = null;
        //phoneStateListener = null;

        Log.d(PACKAGE_NAME, "AlwaysOnTop : AoT 소멸");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(PACKAGE_NAME, "AlwaysOnTop : onStartCommand");
        OnTop_view.setSystemUiVisibility(ui_Options);

        Notification.Builder mBuilder = new Notification.Builder(getApplicationContext());

        mBuilder.setContentTitle("당근과 채찍 실행 중")
                .setContentText("")
                .setSmallIcon(R.drawable.carrot_noti);

        startForeground(0, mBuilder.build());
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);

        Log.d(PACKAGE_NAME, "AlwaysOnTop : onStartCommand : startForeground 호출!!!");

        return super.onStartCommand(intent, flags, startId);

    }


    private void calculate_bar_graph(ArrayList<ArrayList<String>> history) {

        Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph");

        Calendar date_before = Calendar.getInstance();
        Calendar date_after = Calendar.getInstance();

        date_before.add(Calendar.DATE, -7);
        date_before.set(Calendar.HOUR, 0);
        date_before.set(Calendar.MINUTE, 0);
        date_before.set(Calendar.SECOND, 0);

        date_after.add(Calendar.DATE, -6);
        date_after.set(Calendar.HOUR, 0);
        date_after.set(Calendar.MINUTE, 0);
        date_after.set(Calendar.SECOND, 0);

        int flag = -1;

        if ((graph_dataset_today==null)||(graph_dataset_past == null)) {

            Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " + ((graph_dataset_today==null)||(graph_dataset_past == null)));

            graph_dataset_past = new ArrayList<>();
            graph_dataset_today = new ArrayList<>();

            for (int i = history.size()-1; i >= 0; i--) {

                while ((date_before.getTimeInMillis()/1000 > Long.valueOf(history.get(i).get(0)))
                        ||((Long.valueOf(history.get(i).get(0)) >= date_after.getTimeInMillis()/1000))) {

                    flag++;

                    if (flag != 6)
                        graph_dataset_past.add(new BarEntry(flag, 0));
                    else
                        graph_dataset_today.add(new BarEntry(0, 0));

                    date_before.add(Calendar.DATE, 1);
                    date_after.add(Calendar.DATE, 1);

                }


                if (((history.get(i).get(3).equals("정산"))||(history.get(i).get(3).equals("비정산 정산")))
                        && (history.get(i).get(1).equals("-"))) {

                    if (flag != 6) {
                        graph_dataset_past.get(flag).setY(
                                graph_dataset_past.get(flag).getY() + Float.valueOf(history.get(i).get(2))
                        );
                    } else {
                        graph_dataset_today.get(0).setY(
                                graph_dataset_today.get(0).getY() + Float.valueOf(history.get(i).get(2))
                        );
                    }

                }
            }

            /*for (int i = history.size()-1; i >= 0; i--) {

                Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " +
                    history.get(i).get(3) + " | " + history.get(i).get(2));

                if (Long.valueOf(history.get(i).get(0)) > (date.getTimeInMillis()/1000)) {

                    Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " +
                            date.getTimeInMillis()/1000);

                    date.add(Calendar.DATE, 1);
                    flag++;

                    if (flag != 6)
                        graph_dataset_past.add(new BarEntry(flag, 0));
                    else
                        graph_dataset_today.add(new BarEntry(0, 0));
                }

                if (((history.get(i).get(3).equals("정산"))||(history.get(i).get(3).equals("비정산 정산")))
                        && (history.get(i).get(1).equals("-"))) {

                    if (flag != 6) {
                        graph_dataset_past.get(flag).setY(
                                graph_dataset_past.get(flag).getY() + Float.valueOf(history.get(i).get(2))
                        );
                    } else {
                        graph_dataset_today.get(0).setY(
                                graph_dataset_today.get(0).getY() + Float.valueOf(history.get(i).get(2))
                        );
                    }

                }

            }*/

        } else {

            // 필요 없음 ㅎㅎ;ㅎ;ㅎ;ㅎ;
            /*Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " + ((graph_dataset_today==null)||(graph_dataset_past == null)));
            Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " + history.get(0).get(2));
            Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " + graph_dataset_today.get(0).getY());
            Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " + graph_dataset_today.get(0).getY() + Long.valueOf(history.get(0).get(2)));



            if (((history.get(0).get(3).equals("정산"))||(history.get(0).get(3).equals("비정산 정산")))
                    && (history.get(0).get(1).equals("-"))) {

                graph_dataset_today.get(0).setY(
                        graph_dataset_today.get(0).getY() + Long.valueOf(history.get(0).get(2))
                );

            }*/

        }

        Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " + graph_dataset_past);
        Log.d(PACKAGE_NAME, "AlwaysOnTop : calculate_bar_graph : " + graph_dataset_today);

        init_bar_graph();

    }


    private void init_bar_graph() {



        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : " + graph_dataset_past);



        BarDataSet set1 = new BarDataSet(graph_dataset_past, null);
        set1.setColors(Color.rgb(180,180,180));

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        //data.setBarWidth(0.9f);

        bar_graph_past.setTouchEnabled(false);
        bar_graph_past.setDragEnabled(false);

        bar_graph_past.getXAxis().setDrawGridLines(false);
        bar_graph_past.getXAxis().setDrawAxisLine(false);
        bar_graph_past.getXAxis().setDrawLabels(false);
        bar_graph_past.getXAxis().setTextSize(15f);
        bar_graph_past.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        bar_graph_past.getAxisLeft().setDrawGridLines(false);
        bar_graph_past.getAxisLeft().setDrawAxisLine(false);
        bar_graph_past.getAxisLeft().setDrawLabels(false);

        bar_graph_past.getAxisRight().setDrawGridLines(false);
        bar_graph_past.getAxisRight().setDrawAxisLine(false);
        bar_graph_past.getAxisRight().setDrawLabels(false);

        bar_graph_past.setDrawValueAboveBar(true);
        bar_graph_past.setDrawMarkers(false);
        bar_graph_past.setDescription(null);

        bar_graph_past.getLegend().setEnabled(false);
        bar_graph_past.setData(data);



        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : " + graph_dataset_today);



        BarDataSet set2 = new BarDataSet(graph_dataset_today, null);
        set2.setColors(Color.rgb(255, 121, 0));

        ArrayList<IBarDataSet> dataSets2 = new ArrayList<>();
        dataSets2.add(set2);

        BarData data_today = new BarData(dataSets2);
        data_today.setBarWidth(20f);
        data_today.setValueTextSize(15f);

        bar_graph_today.setTouchEnabled(false);
        bar_graph_today.setDragEnabled(false);

        bar_graph_today.getXAxis().setDrawGridLines(false);
        bar_graph_today.getXAxis().setDrawAxisLine(false);
        bar_graph_today.getXAxis().setDrawLabels(false);
        bar_graph_today.getXAxis().setTextSize(20f);
        bar_graph_today.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);

        bar_graph_today.getAxisLeft().setDrawGridLines(false);
        bar_graph_today.getAxisLeft().setDrawAxisLine(false);
        bar_graph_today.getAxisLeft().setDrawLabels(false);

        bar_graph_today.getAxisRight().setDrawGridLines(false);
        bar_graph_today.getAxisRight().setDrawAxisLine(false);
        bar_graph_today.getAxisRight().setDrawLabels(false);

        bar_graph_today.setDrawValueAboveBar(true);
        bar_graph_today.setDrawMarkers(false);
        bar_graph_today.setDescription(null);

        bar_graph_today.getLegend().setEnabled(false);
        bar_graph_today.setData(data_today);

        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_past : X : " +
                bar_graph_past.getXAxis().getAxisMinimum() + "|" +
                bar_graph_past.getXAxis().getAxisMaximum());
        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_past : Y : " +
                bar_graph_past.getAxisLeft().getAxisMinimum() + "|" +
                bar_graph_past.getAxisLeft().getAxisMaximum());
        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_today : X : " +
                bar_graph_today.getXAxis().getAxisMinimum() + "|" +
                bar_graph_today.getXAxis().getAxisMaximum());
        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_today : Y : " +
                bar_graph_today.getAxisLeft().getAxisMinimum() + "|" +
                bar_graph_today.getAxisLeft().getAxisMaximum());

        bar_graph_past.getAxisLeft().setAxisMinimum(0);
        bar_graph_past.getAxisRight().setAxisMinimum(0);
        bar_graph_today.getAxisLeft().setAxisMinimum(0);
        bar_graph_today.getAxisRight().setAxisMinimum(0);

        if (Float.isInfinite(bar_graph_past.getAxisLeft().getAxisMaximum()))
            bar_graph_past.getAxisLeft().setAxisMaximum(0);
        if (Float.isInfinite(bar_graph_today.getAxisLeft().getAxisMaximum()))
            bar_graph_today.getAxisLeft().setAxisMaximum(0);

        if (bar_graph_past.getAxisLeft().getAxisMaximum() > bar_graph_today.getAxisLeft().getAxisMaximum())
            bar_graph_today.getAxisLeft().setAxisMaximum(bar_graph_past.getAxisLeft().getAxisMaximum());
        else{
            bar_graph_today.getAxisLeft().setAxisMaximum(bar_graph_today.getAxisLeft().getAxisMaximum() + 400);
            bar_graph_past.getAxisLeft().setAxisMaximum(bar_graph_today.getAxisLeft().getAxisMaximum() + 400);
        }

        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_past : X : " +
                bar_graph_past.getXAxis().getAxisMinimum() + "|" +
                bar_graph_past.getXAxis().getAxisMaximum());
        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_past : Y : " +
                bar_graph_past.getAxisLeft().getAxisMinimum() + "|" +
                bar_graph_past.getAxisLeft().getAxisMaximum());
        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_today : X : " +
                bar_graph_today.getXAxis().getAxisMinimum() + "|" +
                bar_graph_today.getXAxis().getAxisMaximum());
        Log.d(PACKAGE_NAME, "AlwaysOnTop : init_bar_graph : bar_graph_today : Y : " +
                bar_graph_today.getAxisLeft().getAxisMinimum() + "|" +
                bar_graph_today.getAxisLeft().getAxisMaximum());

        //bar_graph_past.notify();
        //bar_graph_today.notify();

        //bar_graph_past.notifyAll();
        //bar_graph_today.notifyAll();

        bar_graph_past.invalidate();
        bar_graph_today.invalidate();

        bar_graph_past.notifyDataSetChanged();
        bar_graph_today.notifyDataSetChanged();

    }


    public void start_creditticker() {
        Log.d(PACKAGE_NAME, "AlwaysOnTop : start_creditticker");

        if (user_credit <= 0){
            if (user_credit == -1) Toast.makeText(this, "현재 로딩 중입니다.\n잠시만 기다려주세요", Toast.LENGTH_SHORT).show();
            if (user_credit == 0) Toast.makeText(this, "Credit이 0으로 사용할 수 없습니다.\nCredit을 쌓아보세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        //SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        //SharedPreferences.Editor editor = pref.edit();

        //Calendar now_time = Calendar.getInstance();
        //editor.putString("startTIME", now_time.getTime().toString());

        //Log.d(PACKAGE_NAME, "AlwaysOnTop : 사용시작 : "+now_time.getTime().toString());

        //editor.commit();

        //////

        //Log.d(PACKAGE_NAME, "AlwaysOnTop : CreditTickerService 생성");
        //Intent i = new Intent(AlwaysOnTop.this, CreditTickerService.class);
        //i.putExtra("Credit", user_credit);
        //startService(i);

        //////

        sendMessage(197, null);
    }
    public void close_AoT_service() {
        Log.d(PACKAGE_NAME, "AlwaysOnTop : AoT screen setSystemUiVisibility 해제");
        OnTop_view.setOnSystemUiVisibilityChangeListener(null);
        Log.d(PACKAGE_NAME, "AlwaysOnTop : onSystemUiVisibilityChange_exit");
        OnTop_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        sendMessage(199, null);
    }

    public void send_history(ArrayList<ArrayList<String>> history) {
        calculate_bar_graph(history);
        history_adapter.setHistory(history);
        //history_adapter.notifyDataSetChanged();
    }

    public class AdapterAOT extends PagerAdapter {

        final String PACKAGE_NAME = "Carrot_and_Stick";
        int[] aot_screen = {
                R.layout.aot_setting,
                R.layout.aot_main,
                R.layout.aot_history
        };

        Context mContext;

        public AdapterAOT(Context context) {
            this.mContext = context;
        }

        @TargetApi(21)
        public View moveup_layout(View layout) {

            Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : elevation adopted!!");
            layout.setElevation((float) 10.0);
            return layout;

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = LayoutInflater.from(mContext);

            Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiating : " + position);
            View layout = (View) inflater.inflate(aot_screen[position], container, false);
            layout.setBackgroundResource(R.drawable.aot_corner);

            layout = moveup_layout(layout);

            container.addView(layout);

            switch (position) {
                case 0:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiateItem : 0(setting)");
                    aot_setting = (ListView) layout.findViewById(R.id.AoT_setting);
                    setting_adapter = new AdapterSetting(getBaseContext());
                    aot_setting.setAdapter(setting_adapter);
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : setting initialized");
                    break;
                case 1:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiateItem : 1(main)");
                    bar_graph_past = (BarChart) layout.findViewById(R.id.bar_graph_past);
                    bar_graph_today = (BarChart) layout.findViewById(R.id.bar_graph_today);
                    /// temporary ///
                    //init_bar_graph();
                    /////////////////

                    aot_custom_slidinglayout = (LayoutSliding) layout.findViewById(R.id.aot_custom_slidinglayout);
                    aot_custom_slidinglayout.onPhoneStateListener(LayoutSliding.PHONE_STATE_NOT_YET, String.valueOf(AoT_MaintextColor));
                    //image_phonestate = (ImageView) layout.findViewById(R.id.image_phonestate);
                    //text_phonestate1 = (TextView) layout.findViewById(R.id.text_phonestate1);
                    //text_phonestate2 = (TextView) layout.findViewById(R.id.text_phonestate2);
                    //manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    //manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

                    //시계시계//
                    tv_time = (TextView) layout.findViewById(R.id.tv_time);
                    timer_start();
                    ////////////
                    break;
                case 2:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiateItem : 2(history)");
                    PB3 = (ProgressBar) layout.findViewById(R.id.progress_bar3);
                    aot_history = (ListView) layout.findViewById(R.id.AoT_history);
                    history_adapter = new AdapterHistory(getBaseContext());
                    aot_history.setAdapter(history_adapter);
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : history initialized");
                    break;
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
                Spannable sp = new SpannableString(time_format.format(now_time.getTime()));
                sp.setSpan(new RelativeSizeSpan(0.7f), sp.length()-3, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                tv_time.setText(sp);
            }
        };

        handler.post(updater);
    }


    public void credit_updown_effect(final TextView textView, int mode) {
        if (textView == null) return;

        ObjectAnimator animator = null;

        switch (mode) {
            case 1: /// UP
                iv_main_credit.setImageResource(R.drawable.up_image_1);
                animator = ObjectAnimator.ofInt(textView, "textColor", Color.RED, AoT_MaintextColor);
                break;
            case -1: /// DONW
                iv_main_credit.setImageResource(R.drawable.down_image_1);
                animator = ObjectAnimator.ofInt(textView, "textColor", Color.BLUE, AoT_MaintextColor);
                break;
        }
        iv_main_credit.setAlpha((float) 1.0);

        Animation fadeInAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        fadeInAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                iv_main_credit.setAlpha((float) 0.0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animator.setDuration(500L);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setInterpolator(new DecelerateInterpolator(2));
        animator.setRepeatCount(3);
        animator.setRepeatMode(ValueAnimator.RESTART);

        iv_main_credit.startAnimation(fadeInAnimation);
        animator.start();
    }



    public void off_screen() {
        ComponentName adminComponent = new ComponentName(getApplicationContext(), ReceiverDeviceAdmin.class);
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        devicePolicyManager.lockNow();
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
            Log.d(PACKAGE_NAME, "AlwaysOnTop : mConnection_background : connected");
            mService_background = new Messenger(iBinder);
            mBound_background = true;
            ///// for reconnect /////
            sendMessage(-1, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(PACKAGE_NAME, "AlwaysOnTop : mConnection_background : disconnected");
            mService_background = null;
            mBound_background = false;
        }
    };


    public void add_to_message_queue(int what, String extra_data) {
        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : add_to_message_queue!! : " + what + " : " + extra_data);

        ArrayList<String> message = new ArrayList<>();
        message.add(String.valueOf(what));
        message.add(extra_data);

        message_list.add(message);

        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : add_to_message_queue size : " + message_list.size());
    }


    ////////////////////////////////////////////////////////////////////////////////////ver.170115//
    /////        ////          ////   //////  ////       ///////////// Background Service///////////
    ///  ////////////  ////////////  /  ////  ////  ////  /////////// AoT connected      : 101 /////
    ////        /////          ////  ///  //  ////  /////  ////////// AoT req CREDIT     : 102 /////
    //////////   ////  ////////////  /////    ////  ////  /////////// AoT req SETTING    : 103 /////
    ///        //////          ////  ///////  ////       //////////// AoT req LOG info   : 104 /////
    ///////////////////////////////////////////////////////////////// AoT req PHONESTATE : 151 /////
    ///////////////////////////////////////////////////////////////// AoT diconn req w/o : 196 /////
    ///////////////////////////////////////////////////////////////// AoT diconn req     : 197 /////
    ///////////////////////////////////////////////////////////////// AoT end msg        : 199 /////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public synchronized void sender_unit(Message msg) {

        if (msg.what == -1) return;

        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : sender_unit started " + msg.what + " | " + msg.getData());

        try {
            mService_background.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }


    public void sendMessage(int what, String extra_data) {

        if (what == 0) return;

        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : sendMessage = " + mBound_background + " : " + what);
        if (!mBound_background) {
            if (what != -1)
                add_to_message_queue(what, extra_data);

            bindService(new Intent(this, ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
        }
        else {
            while(message_list.size() != 0) {
                Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : queue_list_size : " + message_list.size());
                Message msg = Message.obtain(null, Integer.valueOf(message_list.get(0).get(0)), 0, 0);

                if (message_list.get(0).get(1) != null) {
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : queue : " + message_list.get(0).get(0) + " | " + message_list.get(0).get(1));

                    Bundle data = new Bundle();
                    data.putString("extra_data", message_list.get(0).get(1));
                    msg.setData(data);
                } else {
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : queue : " + message_list.get(0).get(0));
                }

                sender_unit(msg);

                message_list.remove(0);
            }

            Message msg = Message.obtain(null, what, 0, 0);

            if (extra_data != null) {
                Bundle data = new Bundle();
                data.putString("extra_data" , extra_data);
                msg.setData(data);
            }

            sender_unit(msg);

            if (what == 199) {
                unbindService(mConnection_background);
                mConnection_background.onServiceDisconnected(null);

                Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : 나 이제 죽는다?");
                stopSelf();
            }
        }
    }


    ////////////////////////ver.170115//
    ///// Connect w/AoT      : 100 /////
    ///// send Credit        : 102 /////
    ///// send Setting       : 103 /////
    ///// send History       : 104 /////
    ///// AoT req PHONESTATE : 151 /////
    ///// Disconnect w/AoT   : 198 /////
    ////////////////////////////////////
    ///// FAIL!! resend code : 999 /////
    ////////////////////////////////////
    @Override
    public IBinder onBind(Intent intent) {
        return AoTMessenger.getBinder();
    }
    final Messenger AoTMessenger = new Messenger(new AoTIncomingHandler());
    class AoTIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : BackgroundIncomingHandler = " + msg.what);

            switch (msg.what) {
                case 100:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : BackgroundIncomingHandler : Background connected");
                    ServiceAlwaysOnTop.this.sendMessage(102, null);
                    ServiceAlwaysOnTop.this.sendMessage(103, null);
                    ServiceAlwaysOnTop.this.sendMessage(104, null);
                    ServiceAlwaysOnTop.this.sendMessage(151, null);
                    break;
                case 102:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : BackgroundIncomingHandler = " + msg.getData().getString("extra_data") + " : " + user_credit);

                    btn_close.setText("Credit\n사용하기");
                    btn_close.setEnabled(true);
                    PB1.setVisibility(View.INVISIBLE);
                    PB2.setVisibility(View.INVISIBLE);

                    if (user_credit != Integer.valueOf(msg.getData().getString("extra_data"))) {
                        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : 102 : 뙇1 : " + msg.getData().getString("extra_data").toString());
                        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : 102 : 뙇2 : " + (user_credit > Integer.valueOf(msg.getData().getString("extra_data").toString())));

                        if (user_credit > Integer.valueOf(msg.getData().getString("extra_data").toString()))
                            credit_updown_effect(tv_main_credit, -1);
                        else
                            credit_updown_effect(tv_main_credit, 1);

                        editor.putInt("user_credit", Integer.valueOf(msg.getData().getString("extra_data").toString()));
                        editor.commit();
                    }

                    tv_main_credit.setText(msg.getData().getString("extra_data").toString());
                    user_credit = Integer.valueOf(tv_main_credit.getText().toString());

                    break;
                case 103:
                    //unser constuction
                    break;
                case 104:
                    ArrayList received_history = (ArrayList) msg.getData().getSerializable("log_init");
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : received_history_size : " + received_history.size());
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : received_history_latest : " + received_history.get(0));

                    ServiceAlwaysOnTop.this.send_history(received_history);

                    PB3.setVisibility(View.GONE);
                    aot_history.setVisibility(View.VISIBLE);
                    break;
                case 151:
                    if (aot_custom_slidinglayout == null) {
                        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : onCallStateChanged | aot_custom_slidinglayout is not initialized");
                        ServiceAlwaysOnTop.this.sendMessage(999, "151");
                    }
                    else {
                        if (aot_custom_slidinglayout.isAllInitialized()) {
                            Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : onCallStateChanged | incomming :" + msg.getData().getString("extra_data") + " | " + msg.getData().getString("call_number"));
                            aot_custom_slidinglayout.onPhoneStateListener(Integer.parseInt(msg.getData().getString("extra_data")), msg.getData().getString("call_number"));

                            if (msg.getData().getString("extra_data").equals("1")) {
                                ServiceAlwaysOnTop.this.sendMessage(196, null);
                            }

                        } else {
                            Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : onCallStateChanged | aot_custom_slidinglayout's components are not initialized");
                            ServiceAlwaysOnTop.this.sendMessage(999, "151");
                        }
                        //isNowOutgoing = true;
                    }
                    break;
                case 198:
                    ServiceAlwaysOnTop.this.close_AoT_service();
                    break;

                case 999:
                    ServiceAlwaysOnTop.this.sendMessage(Integer.valueOf(msg.getData().getString("extra_data")), null);
            }

        }
    }

}
