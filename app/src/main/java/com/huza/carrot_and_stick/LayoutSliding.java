package com.huza.carrot_and_stick;

import android.animation.Animator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by HuZA on 2016-11-09.
 */

public class LayoutSliding extends LinearLayout {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    Context mContext;
    View thisView;

    int height, width;
    int[] location = new int[2];
    int release_y, reference_y = -1;

    boolean isOpened;

    ImageView btn_init;

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

    public void init() {
        Log.d(PACKAGE_NAME, "SlidingLayout : init called");

        thisView = this;
        thisView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(PACKAGE_NAME, "SlidingLayout : post called");


                calculate_location();


                Log.d(PACKAGE_NAME, "SlidingLayout : 접었음");

                thisView.animate()
                        .translationYBy(height*1/2)
                        .setDuration(250)
                        .setStartDelay(250)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                thisView.animate()
                                        .translationYBy(-height*1/2)
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
                isOpened = false;

                btn_init = (ImageView) thisView.findViewById(R.id.image_phonestate);
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
            }
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.d(PACKAGE_NAME, "SlidingLayout : onSizeChanged");
        super.onSizeChanged(w, h, oldw, oldh);
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

    public void now_CALL_STATE_OFFHOOK() {

        Log.d(PACKAGE_NAME, "SlidingLayout : now_CALL_STATE_OFFHOOK");
        //image_phonestate.setImageResource(R.drawable.phone_state_offhook);
        //text_phonestate.setText("전화 통화 중");
        open_slide();
        kill_timer();

    }

    public void now_CALL_STATE_RINGING() {

        Log.d(PACKAGE_NAME, "SlidingLayout : now_CALL_STATE_RINGING");
        //image_phonestate.setImageResource(R.drawable.phone_state_ringing);
        //text_phonestate.setText("전화 수신 중");
        open_slide();
        kill_timer();

    }

    public void now_CALL_STATE_IDLE() {

        Log.d(PACKAGE_NAME, "SlidingLayout : now_CALL_STATE_IDLE");
        //image_phonestate.setImageResource(R.drawable.phone_state_ringing);
        //text_phonestate.setText("전화 수신 중");
        close_slide();

    }


    TimerTask timertask;
    Timer timer = new Timer();
    Handler handler = new Handler();

    private void timer_start() {

        Log.d(PACKAGE_NAME, "timer_start : start");

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
        Log.d(PACKAGE_NAME, "timer_start : times_up");
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
        Log.d(PACKAGE_NAME, "timer_start : kill_timer");
    }

}
