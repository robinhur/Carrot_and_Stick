package com.huza.carrot_and_stick;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextView;

import com.android.internal.telephony.ITelephony;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by HuZA on 2016-11-09.
 */

public class LayoutSliding extends LinearLayout {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    static final int PHONE_STATE_NOT_YET = -1;
    static final int PHONE_STATE_IDLE = 0;
    static final int PHONE_STATE_RINGING = 1;
    static final int PHONE_STATE_OUTGOING = 2;
    static final int PHONE_STATE_OFFHOOK = 3;

    int AoT_MaintextColor;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    Context mContext;
    View thisView;

    int height, width;
    int[] location = new int[2];
    int release_y, reference_y = -1;

    boolean isOpened;

    ImageView btn_init;

    TextView text_phonestate1;
    TextView text_phonestate2;

    Button call_start;
    Button call_end;
    Space call_space;
    TextView call_number;
    TextView call_state;

    public LayoutSliding(Context context) {
        super(context);
        mContext = context;
        init();
    }
    public LayoutSliding(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public String change_NUMBER(String number) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return PhoneNumberUtils.formatNumber(number, Locale.getDefault().getCountry());
        } else {
            return number;
        }
    }

    public void calculate_location() {
        width = thisView.getMeasuredWidth();
        height = thisView.getMeasuredHeight();

        thisView.getLocationOnScreen(location);

        Log.d(PACKAGE_NAME, "SlidingLayout : height : " + height);
        Log.d(PACKAGE_NAME, "SlidingLayout : width : " + width);

        thisView.clearAnimation();
        thisView.setTranslationY(-height*1/2);
        thisView.setVisibility(VISIBLE);
    }
    public void close_slide(){
        Log.d(PACKAGE_NAME, "SlidingLayout : close_slide");
        //thisView.setTranslationY(0);
        thisView.animate()
                .translationY(-height*1/2)
                .setDuration(200)
                .start();
        isOpened = false;
    }
    public void open_slide(){
        Log.d(PACKAGE_NAME, "SlidingLayout : open_slide");
        //thisView.setTranslationY(-height*3/4);
        thisView.animate()
                .translationY(0)
                .setDuration(200)
                .start();
        isOpened = true;
        timer_start();
    }
    public void init() {

        Log.d(PACKAGE_NAME, "SlidingLayout : init called");

        pref = getContext().getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        editor = pref.edit();

        thisView = this;
        thisView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(PACKAGE_NAME, "SlidingLayout : post called | outgoing_NUMBER = " + pref.getString("outgoing_NUMBER", "!"));

                btn_init = (ImageView) thisView.findViewById(R.id.image_phonestate);
                text_phonestate1 = (TextView) thisView.findViewById(R.id.text_phonestate1);
                text_phonestate2 = (TextView) thisView.findViewById(R.id.text_phonestate2);

                call_number = (TextView) thisView.findViewById(R.id.aot_sliding_call_number);
                call_state = (TextView) thisView.findViewById(R.id.aot_sliding_call_state);

                call_start = (Button) thisView.findViewById(R.id.aot_sliding_call_start);
                call_start.setText("전화 걸기");
                call_end = (Button) thisView.findViewById(R.id.aot_sliding_call_end);
                call_end.setHeight(0);
                call_end.setVisibility(GONE);
                call_space = (Space) thisView.findViewById(R.id.aot_sliding_call_space);

                call_start.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        Log.d(PACKAGE_NAME, "SlidingLayout : call_start!!!!!!!!! : onClick | " + ((Button)view).getText().toString());

                        switch (((Button)view).getText().toString()) {
                            case "전화 걸기" :

                                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:010-5784-4785"));
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                                if (ActivityCompat.checkSelfPermission(mContext, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }

                                mContext.startActivity(intent);
                                break;

                            case "전화 받기" :

                                Log.d(PACKAGE_NAME, "SlidingLayout : call_start : onClick | 전화 받기!!!!!!!!!!");

                                Intent i = new Intent(mContext, ActivityForAcceptCall.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                        | Intent.FLAG_ACTIVITY_NO_HISTORY
                                        | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                                mContext.startActivity(i);

                                /*// Runtime.exec(String) had an I/O problem, try to fall back
                                String enforcedPerm = "android.permission.CALL_PRIVILEGED";
                                Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                                        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                                                KeyEvent.KEYCODE_HEADSETHOOK));
                                Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                                        Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                                                KeyEvent.KEYCODE_HEADSETHOOK));

                                mContext.sendOrderedBroadcast(btnDown, enforcedPerm);
                                mContext.sendOrderedBroadcast(btnUp, enforcedPerm);*/
                                /*
                                try {
                                    Log.d(PACKAGE_NAME, "SlidingLayout : answer | yes");
                                    Runtime.getRuntime().exec("input keyevent " +
                                            Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));
                                } catch (IOException e) {
                                    Log.d(PACKAGE_NAME, "SlidingLayout : answer | no");
                                    answerRingingCallWithIntent();
                                }
                                */

                                /*
                                    Intent buttonDown = new Intent(Intent.ACTION_MEDIA_BUTTON);
                                    buttonDown.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK));
                                    mContext.sendOrderedBroadcast(buttonDown, "android.permission.CALL_PRIVILEGED");
                                */
                                /*
                                    try {
                                        Runtime.getRuntime().exec("input keyevent " + Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));
                                    } catch (IOException e) {
                                        //handle error here
                                    }
                                */
                                /*
                                    try {
                                        TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                                        Class c = Class.forName(telephonyManager.getClass().getName());
                                        Method m = c.getDeclaredMethod("getITelephony");
                                        m.setAccessible(true);
                                        com.android.internal.telephony.ITelephony telephonyService = (ITelephony) m.invoke(telephonyManager);

                                        telephonyService.answerRingingCall();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                */
                                break;

                        }
                    }
                });
                call_end.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        try {
                            TelephonyManager telephonyManager = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                            Class c = Class.forName(telephonyManager.getClass().getName());
                            Method m = c.getDeclaredMethod("getITelephony");
                            m.setAccessible(true);
                            com.android.internal.telephony.ITelephony telephonyService = (ITelephony) m.invoke(telephonyManager);

                            telephonyService.endCall();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                calculate_location();

                if (pref.getString("outgoing_NUMBER", "") == "") {
                    thisView.animate()
                            .translationY(0)
                            .setDuration(250)
                            .setStartDelay(250)
                            .setListener(new Animator.AnimatorListener() {
                                @Override
                                public void onAnimationStart(Animator animator) {

                                }

                                @Override
                                public void onAnimationEnd(Animator animator) {
                                    thisView.animate()
                                            .translationY(-height*1/2)
                                            .setDuration(250)
                                            .setListener(new Animator.AnimatorListener() {
                                                @Override
                                                public void onAnimationStart(Animator animator) {

                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animator) {
                                                    thisView.setVisibility(VISIBLE);
                                                }

                                                @Override
                                                public void onAnimationCancel(Animator animator) {

                                                }

                                                @Override
                                                public void onAnimationRepeat(Animator animator) {

                                                }
                                            })
                                            .start();
                                }

                                @Override
                                public void onAnimationCancel(Animator animator) {

                                }

                                @Override
                                public void onAnimationRepeat(Animator animator) {

                                }
                            })
                            .start();

                    Log.d(PACKAGE_NAME, "SlidingLayout : init 애니메이션 끝");
                }

                isOpened = false;

                btn_init.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        //// Y pos : ((int)motionEvent.getRawY() - location[1])
                        //// start : height / 4
                        //// end : height

                        Log.d(PACKAGE_NAME, "SlidingLayout : OnTouchListener : (" + (int)motionEvent.getRawX() + ", " + (int)motionEvent.getRawY() + ")  (" + location[0] + ", " + location[1] + ")");
                        release_y = ((int)motionEvent.getRawY() - location[1]);

                        switch (motionEvent.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                Log.d(PACKAGE_NAME, "SlidingLayout : OnTouchListener : ACTION_DOWN");
                                kill_timer();
                                if (reference_y == -1)
                                    reference_y = ((int)motionEvent.getRawY() - location[1]);
                                break;
                            case MotionEvent.ACTION_MOVE:
                                Log.d(PACKAGE_NAME, "SlidingLayout : OnTouchListener : ACTION_MOVE");
                                if (!isOpened) {
                                    if (release_y-reference_y < 0)
                                        thisView.setTranslationY(-height*1/2);
                                    else if (release_y-reference_y > height/2)
                                        thisView.setTranslationY(0);
                                    else
                                        thisView.setTranslationY((-height*1/2) + release_y-reference_y);
                                } else {
                                    if (release_y-reference_y > 0)
                                        thisView.setTranslationY(0);
                                    else if (release_y-reference_y < -height*1/2)
                                        thisView.setTranslationY(-height*1/2);
                                    else
                                        thisView.setTranslationY(release_y-reference_y);
                                }
                                break;
                            case MotionEvent.ACTION_UP:
                                Log.d(PACKAGE_NAME, "SlidingLayout : OnTouchListener : ACTION_UP");
                                if (!isOpened) {
                                    if (release_y-reference_y > height/4 || ((release_y-reference_y < height/8)&&(release_y-reference_y>=0)))
                                        open_slide();
                                    else
                                        close_slide();
                                } else {
                                    if (release_y - reference_y < -height/4 || ((release_y-reference_y > -height/8)&&(release_y-reference_y<=0)))
                                        close_slide();
                                    else
                                        open_slide();
                                }
                                reference_y = -1;
                                break;
                        }

                        return false;
                    }

                });

                //if (pref.getString("outgoing_NUMBER", "") != "") {
                //    now_NEW_OUTGOING_CALL(pref.getString("outgoing_NUMBER", ""));
                //}
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(PACKAGE_NAME, "SlidingLayout : onSizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void timer_start() {

        Log.d(PACKAGE_NAME, "SlidingLayout : timer_start : start");

        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        timertask = new TimerTask() {
            @Override
            public void run() {
                times_up();
            }
        };

        timer = new Timer();
        timer.schedule(timertask, 2000);

    }
    private void times_up() {
        Log.d(PACKAGE_NAME, "SlidingLayout : timer_start : times_up");
        Runnable updater = new Runnable() {
            @Override
            public void run() {
                Log.d(PACKAGE_NAME, "timer_start : times_up : run");
                close_slide();
            }
        };

        handler.post(updater);

        kill_timer();
    }
    public void kill_timer() {
        timertask = null;
        timer.cancel();
        timer.purge();
        Log.d(PACKAGE_NAME, "SlidingLayout : timer_start : kill_timer");
    }
    public void answerRingingCallWithIntent() {
        try {
            Log.d(PACKAGE_NAME, "SlidingLayout : answerRingingCallWithIntent | yes");
            Intent localIntent1 = new Intent(Intent.ACTION_HEADSET_PLUG);
            localIntent1.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            localIntent1.putExtra("state", 1);
            localIntent1.putExtra("microphone", 1);
            localIntent1.putExtra("name", "Headset");
            getContext().sendOrderedBroadcast(localIntent1, "android.permission.CALL_PRIVILEGED");

            Intent localIntent2 = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent localKeyEvent1 = new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_HEADSETHOOK);
            localIntent2.putExtra(Intent.EXTRA_KEY_EVENT, localKeyEvent1);
            getContext().sendOrderedBroadcast(localIntent2, "android.permission.CALL_PRIVILEGED");

            Intent localIntent3 = new Intent(Intent.ACTION_MEDIA_BUTTON);
            KeyEvent localKeyEvent2 = new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_HEADSETHOOK);
            localIntent3.putExtra(Intent.EXTRA_KEY_EVENT, localKeyEvent2);
            getContext().sendOrderedBroadcast(localIntent3, "android.permission.CALL_PRIVILEGED");

            Intent localIntent4 = new Intent(Intent.ACTION_HEADSET_PLUG);
            localIntent4.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            localIntent4.putExtra("state", 0);
            localIntent4.putExtra("microphone", 1);
            localIntent4.putExtra("name", "Headset");
            getContext().sendOrderedBroadcast(localIntent4, "android.permission.CALL_PRIVILEGED");
        } catch (Exception e2) {
            Log.d(PACKAGE_NAME, "SlidingLayout : answerRingingCallWithIntent | no");
            e2.printStackTrace();
        }
    }

    public void now_CALL_STATE_OFFHOOK(String incomming_NUMBER) {

        Log.d(PACKAGE_NAME, "SlidingLayout : now_CALL_STATE_OFFHOOK");

        btn_init.setImageResource(R.drawable.phone_state_offhook);
        text_phonestate1.setTextColor(Color.GREEN);
        text_phonestate1.setText("수신 ");
        text_phonestate2.setTextColor(Color.GREEN);
        text_phonestate2.setText("통화 중");
        if (call_end != null) {
            call_end.setVisibility(VISIBLE);
            call_end.setHeight(150);

            ////////////////////////////////
            call_state.setText("수신 번호");
            call_number.setText(change_NUMBER(incomming_NUMBER));
            ////////////////////////////////
        }
        if (call_start != null) {
            call_start.setVisibility(GONE);
        }

        open_slide();
        kill_timer();

    }
    public void now_NEW_OUTGOING_CALL(String outgoing_NUMBER) {

        Log.d(PACKAGE_NAME, "SlidingLayout : now_NEW_OUTGOING_CALL | " + outgoing_NUMBER);

        btn_init.setImageResource(R.drawable.phone_state_offhook);
        text_phonestate1.setTextColor(Color.rgb(255, 153, 0));
        text_phonestate1.setText("발신 ");
        text_phonestate2.setTextColor(Color.GREEN);
        text_phonestate2.setText("통화 중");

        if (call_end == null) Log.d(PACKAGE_NAME, "SlidingLayout : no!!!!!!!!!!!!");

        if (call_end != null) {
            call_end.setVisibility(VISIBLE);
            call_end.setHeight(150);

            ////////////////////////////////
            call_state.setText("발신 번호");
            call_number.setText(change_NUMBER(outgoing_NUMBER));
            ////////////////////////////////
        }
        if (call_start != null) {
            call_start.setVisibility(GONE);
        }

        open_slide();
        kill_timer();

    }
    public void now_CALL_STATE_RINGING(String incomming_NUMBER) {

        Log.d(PACKAGE_NAME, "SlidingLayout : now_CALL_STATE_RINGING");
        if (call_end == null) Log.d(PACKAGE_NAME, "SlidingLayout : no!!!!!!!!!!!!");

        if (call_end != null) {
            call_end.setVisibility(VISIBLE);
            call_end.setHeight(40);

            ////////////////////////////////
            call_state.setText("수신 번호");
            call_number.setText(change_NUMBER(incomming_NUMBER));
            ////////////////////////////////
        }
        if (call_start != null) {
            call_start.setVisibility(VISIBLE);
            call_start.setText("전화 받기");
        }

        open_slide();
        kill_timer();

    }
    public void now_CALL_STATE_IDLE() {

        Log.d(PACKAGE_NAME, "SlidingLayout : now_CALL_STATE_IDLE");

        btn_init.setImageResource(R.drawable.phone_state_idle);
        text_phonestate1.setTextColor(AoT_MaintextColor);
        text_phonestate1.setText("전화 ");
        text_phonestate2.setTextColor(AoT_MaintextColor);
        text_phonestate2.setText("대기 중");

        if (call_end == null) Log.d(PACKAGE_NAME, "SlidingLayout : no!!!!!!!!!!!!");

        if (call_end != null) {
            call_end.setHeight(0);
            call_end.setVisibility(GONE);

            ////////////////////////////////
            call_state.setText("기본 발신번호");
            call_number.setText("010-3315-8130");
            ////////////////////////////////
        }
        if (call_start != null) {
            call_start.setVisibility(VISIBLE);
            call_start.setText("전화 걸기");
        }
        close_slide();

    }

    TimerTask timertask;
    Timer timer = new Timer();
    Handler handler = new Handler();

    public boolean isAllInitialized() {

        Log.d(PACKAGE_NAME, "SlidingLayout : phoneStateListener : isAllInitialized : " + ((btn_init != null) && (text_phonestate1 != null) && (text_phonestate2 != null)));

        if ((btn_init != null) && (text_phonestate1 != null) && (text_phonestate2 != null)) {
            return true;
        } else {
            return false;
        }
    }

    public void onPhoneStateListener(int mode, String number) {

        Log.d(PACKAGE_NAME, "SlidingLayout : phoneStateListener1 : " + mode + " | " + number + " = " + (btn_init != null) + " | " + (text_phonestate1 != null) + " | " + (text_phonestate2 != null));

        switch (mode) {
            case PHONE_STATE_NOT_YET:
                AoT_MaintextColor = Integer.parseInt(number);
                break;
            case PHONE_STATE_IDLE:
                now_CALL_STATE_IDLE();
                break;
            case PHONE_STATE_OFFHOOK:
                now_CALL_STATE_OFFHOOK(number);
                break;
            case PHONE_STATE_OUTGOING:
                now_NEW_OUTGOING_CALL(number);
                break;
            case PHONE_STATE_RINGING:
                //now_CALL_STATE_RINGING(number);
                break;
        }

    }
}
