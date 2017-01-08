package com.huza.carrot_and_stick;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
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
import com.github.mikephil.charting.utils.ColorTemplate;
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

    SharedPreferences pref;
    SharedPreferences.Editor editor;

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
    TelephonyManager manager;
    ImageView image_phonestate;
    TextView text_phonestate1;
    TextView text_phonestate2;

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
    SimpleDateFormat time_format = new SimpleDateFormat("aa hh : mm : ss", Locale.KOREA);
    Calendar now_time;

    TimerTask timertask;
    Timer timer = new Timer();
    Handler handler = new Handler();

    int user_credit = -1;

    PhoneStateListener phoneStateListener;

    public ServiceAlwaysOnTop() {
        Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop 생성");
    }

    int what;
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

        what = 0;
        extra_data = null;
        bindService(new Intent(this, ServiceBackground.class), mConnection_background, Context.BIND_AUTO_CREATE);
        what = 101;
        sendMessage();

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

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : onCallStateChanged | state:" + state
                        + "    (ringing:" + TelephonyManager.CALL_STATE_RINGING
                        + ", offhook:" + TelephonyManager.CALL_STATE_OFFHOOK
                        + ", idle:" + TelephonyManager.CALL_STATE_IDLE + ")"
                        + "|number:" + incomingNumber + " | " + isNowOutgoing + " | " + pref.getString("outgoing_NUMBER", ""));

                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        image_phonestate.setImageResource(R.drawable.phone_state_idle);
                        text_phonestate1.setTextColor(AoT_MaintextColor);
                        text_phonestate1.setText("전화 ");
                        text_phonestate2.setTextColor(AoT_MaintextColor);
                        text_phonestate2.setText("대기 중");
                        aot_custom_slidinglayout.now_CALL_STATE_IDLE();
                        isNowOutgoing = false;
                        //평상시
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        image_phonestate.setImageResource(R.drawable.phone_state_ringing);
                        text_phonestate1.setTextColor(Color.YELLOW);
                        text_phonestate1.setText("전화 ");
                        text_phonestate2.setTextColor(Color.YELLOW);
                        text_phonestate2.setText("수신 중");
                        aot_custom_slidinglayout.now_CALL_STATE_RINGING(incomingNumber);
                        //울리는중
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (!isNowOutgoing && pref.getString("outgoing_NUMBER", "") == "") {
                            image_phonestate.setImageResource(R.drawable.phone_state_offhook);
                            text_phonestate1.setTextColor(Color.GREEN);
                            text_phonestate1.setText("수신 ");
                            text_phonestate2.setTextColor(Color.GREEN);
                            text_phonestate2.setText("통화 중");
                            aot_custom_slidinglayout.now_CALL_STATE_OFFHOOK(incomingNumber);
                            //수신중
                        } else {
                            image_phonestate.setImageResource(R.drawable.phone_state_offhook);
                            text_phonestate1.setTextColor(Color.rgb(255, 153, 0));
                            text_phonestate1.setText("발신 ");
                            text_phonestate2.setTextColor(Color.GREEN);
                            text_phonestate2.setText("통화 중");
                            aot_custom_slidinglayout.now_NEW_OUTGOING_CALL(pref.getString("outgoing_NUMBER", ""));
                            isNowOutgoing = true;

                            editor.remove("outgoing_NUMBER");
                            editor.commit();
                            //발신중
                        }
                        break;
                }
            }
        };
    }
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (OnTop_view != null) {
            w_manager.removeView(OnTop_view);
            OnTop_view = null;
        }

        manager = null;
        phoneStateListener = null;

        Log.d(PACKAGE_NAME, "AlwaysOnTop : AoT 소멸");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(PACKAGE_NAME, "AlwaysOnTop : onStartCommand");
        OnTop_view.setSystemUiVisibility(ui_Options);

        return super.onStartCommand(intent, flags, startId);

    }


    private void init_bar_graph() {

        ArrayList<BarEntry> dataset_past = new ArrayList<>();

        for (int i = 0; i < 6; i ++) {
            dataset_past.add(new BarEntry(i, (float)(Math.random()* 100000)));
        }

        BarDataSet set1 = new BarDataSet(dataset_past, null);
        //set1.setColors(ColorTemplate.MATERIAL_COLORS);

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


        ArrayList<BarEntry> dataset_today = new ArrayList<>();
        dataset_today.add(new BarEntry(6, (float)(Math.random()*1000)));

        BarDataSet set2 = new BarDataSet(dataset_today, null);
        set2.setColors(ColorTemplate.MATERIAL_COLORS);

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

        what = 197;
        sendMessage();
    }
    public void close_AoT_service() {
        Log.d(PACKAGE_NAME, "AlwaysOnTop : AoT screen setSystemUiVisibility 해제");
        OnTop_view.setOnSystemUiVisibilityChangeListener(null);
        Log.d(PACKAGE_NAME, "AlwaysOnTop : onSystemUiVisibilityChange_exit");
        OnTop_view.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);

        what = 199;
        sendMessage();
    }

    public void send_history(ArrayList<ArrayList<String>> history) {
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
                    init_bar_graph();
                    /////////////////

                    aot_custom_slidinglayout = (LayoutSliding) layout.findViewById(R.id.aot_custom_slidinglayout);
                    image_phonestate = (ImageView) layout.findViewById(R.id.image_phonestate);
                    text_phonestate1 = (TextView) layout.findViewById(R.id.text_phonestate1);
                    text_phonestate2 = (TextView) layout.findViewById(R.id.text_phonestate2);
                    manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

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

    boolean isNowOutgoing = false;


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
                sp.setSpan(new RelativeSizeSpan(0.7f), sp.length()-5, sp.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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


    public void aot_test_call(View v) {
        Log.d(PACKAGE_NAME, "AlwaysOnTop : aot_test_call : start");
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:010-1234-5678"));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
        }

        Log.d(PACKAGE_NAME, "AlwaysOnTop : aot_test_call : now outgoing calling");
        startActivity(intent);
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
    ///  ////////////  ////////////  /  ////  ////  ////  /////////// AoT connected      : 101 /////
    ////        /////          ////  ///  //  ////  /////  ////////// AoT diconn req     : 197 /////
    //////////   ////  ////////////  /////    ////  ////  /////////// AoT end msg        : 199 /////
    ///        //////          ////  ///////  ////       ///////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void sendMessage() {
        Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : sendMessage = " + mBound_background + " : " + what);

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

            if (what == 199) {
                unbindService(mConnection_background);
                mConnection_background.onServiceDisconnected(null);

                Log.d(PACKAGE_NAME, "AlwaysOnTop : MESSAGE : 나 이제 죽는다?");
                stopSelf();
            }

            what = 0;
            extra_data = null;
        }
    }


    ////////////////////////ver.161228//
    ///// Connect w/AoT      : 100 /////
    ///// send Credit        : 102 /////
    ///// send Setting       : 103 /////
    ///// send History       : 104 /////
    ///// alert OutgoingCall : 152 /////
    ///// Disconnect w/AoT   : 198 /////
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

                    send_history(received_history);

                    PB3.setVisibility(View.GONE);
                    aot_history.setVisibility(View.VISIBLE);
                    break;
                case 152:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : onCallStateChanged | incomming :" + msg.getData().getString("extra_data").toString());
                    isNowOutgoing = true;
                    break;
                case 198:
                    close_AoT_service();
                    break;
            }

        }
    }

}