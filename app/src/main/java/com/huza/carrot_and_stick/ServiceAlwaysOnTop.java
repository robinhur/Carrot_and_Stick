package com.huza.carrot_and_stick;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.matthewtamlin.sliding_intro_screen_library.indicators.DotIndicator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HuZA on 2016-11-14.
 */

public class ServiceAlwaysOnTop extends Service {

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.AlwaysOnTop";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.CreditTickerService";

    View OnTop_view;

    AdapterAOT aot_adapter;
    ViewPager aot_pager;
    DotIndicator aot_indicator;

    TelephonyManager manager;
    ImageView image_phonestate;

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

    int user_credit;

    public ServiceAlwaysOnTop() {
        Log.d(PACKAGE_NAME, "ServiceAlwaysOnTop 생성");
        user_credit = -1;
    }

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
        super.onCreate();

        ///홈버튼누르기///
        gotoHomeScreen();
        //////////////////

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        OnTop_view = inflater.inflate(R.layout.service_alwaysontop, null);

        aot_pager = (ViewPager) OnTop_view.findViewById(R.id.AOT_viewpager);
        aot_adapter = new AdapterAOT(getBaseContext());
        aot_pager.setClipToPadding(false);
        aot_pager.setPadding(100,15,100,15);
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
        final Button btn_close = (Button) OnTop_view.findViewById(R.id.button);
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //close_AoT_service();
            }
        });
        ///////////////////////

        tv_time = (TextView) OnTop_view.findViewById(R.id.tv_time);
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

                //Log.d(PACKAGE_NAME, "AlwaysOnTop : onSystemUiVisibilityChange_listen : " + OnTop_view.getWindowSystemUiVisibility());
            }
        });
        ////////////////////////////////
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

            Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : moved up!!");
            layout.setElevation((float)10.0);
            return layout;

        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            LayoutInflater inflater = LayoutInflater.from(mContext);

            Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiating : "+ position);
            View layout = (View) inflater.inflate(aot_screen[position], container, false);
            layout.setBackgroundResource(R.drawable.aot_corner);

            layout = moveup_layout(layout);

            container.addView(layout);

            switch(position) {
                case 0:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiateItem : 0(setting)");
                    aot_setting = (ListView) layout.findViewById(R.id.AoT_setting);
                    setting_adapter = new AdapterSetting(getBaseContext());
                    aot_setting.setAdapter(setting_adapter);
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : setting initialized");
                    break;
                case 1:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiateItem : 1(main)");
                    image_phonestate = (ImageView) layout.findViewById(R.id.image_phonestate);
                    manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
                    break;
                case 2:
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : instantiateItem : 2(history)");
                    aot_history = (ListView) layout.findViewById(R.id.AoT_history);
                    history_adapter = new AdapterHistory(getBaseContext());
                    aot_history.setAdapter(history_adapter);
                    Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : history initialized");
                    /// add listener in listview from firebase
                    //initLOGListener();
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

    private PhoneStateListener phoneStateListener = new PhoneStateListener() {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch(state){
                case TelephonyManager.CALL_STATE_IDLE:
                    image_phonestate.setImageResource(R.drawable.phone_state_idle);
                    //평상시
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    image_phonestate.setImageResource(R.drawable.phone_state_offhook);
                    //전화중
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    image_phonestate.setImageResource(R.drawable.phone_state_ringing);
                    //울리는중
                    break;
            }

            Log.d(PACKAGE_NAME, "AlwaysOnTop : AOTAdapter : onCallStateChanged | state:" + state
                    + "(ringing:" + TelephonyManager.CALL_STATE_RINGING
                    + ", offhook:" + TelephonyManager.CALL_STATE_OFFHOOK
                    + ", idle:" + TelephonyManager.CALL_STATE_IDLE + ")"
                    + "|number:" + incomingNumber + "|");
        }
    };

}
