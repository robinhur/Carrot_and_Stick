package com.huza.carrot_and_stick;

import android.animation.Animator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by HuZA on 2016-11-09.
 */

public class SlidingLayout extends LinearLayout {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    Context mContext;
    View thisView;

    int height, width;
    int[] location = new int[2];
    int release_y;

    ImageView btn_init;
    Boolean isOpened;

    public SlidingLayout(Context context) {
        super(context);
        mContext = context;
        init();
    }
    public SlidingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public void check_area() {

        width = thisView.getMeasuredWidth();
        height = thisView.getMeasuredHeight();

        Log.d(PACKAGE_NAME, "SlidingLayout : height : " + height);
        Log.d(PACKAGE_NAME, "SlidingLayout : width : " + width);

        thisView.getLocationOnScreen(location);

        Log.d(PACKAGE_NAME, "SlidingLayout : (x,y) : " + location[0] + " , " + location[1]);
    }

    public void init() {

        Log.d(PACKAGE_NAME, "SlidingLayout : init called");

        thisView = this;

        thisView.post(new Runnable() {
            @Override
            public void run() {
                Log.d(PACKAGE_NAME, "SlidingLayout : post called");

                thisView.clearAnimation();
                thisView.setTranslationY(-height*3/4);
                thisView.setVisibility(VISIBLE);

                Log.d(PACKAGE_NAME, "SlidingLayout : 접었음");

                thisView.animate()
                        .translationYBy(height*3/4)
                        .setDuration(250)
                        .setStartDelay(250)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animator) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animator) {
                                thisView.animate()
                                        .translationYBy(-height*3/4)
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

                btn_init = (ImageView) thisView.findViewById(R.id.btn_init);
                btn_init.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        //// Y pos : ((int)motionEvent.getRawY() - location[1])
                        //// start : height / 4
                        //// end : height

                        release_y = ((int)motionEvent.getRawY() - location[1]);

                        if (release_y < height/4)
                            thisView.setTranslationY(-height*3/4);
                        else if (release_y > height)
                            thisView.setTranslationY(0);
                        else {
                            thisView.setTranslationY((-height*3/4) + (release_y-height/4));
                        }

                        return false;
                    }

                });
            }
        });
    }

    public void open_slide(View v){

        if (!isOpened) {
            //thisView.setTranslationY(-height*3/4);
            thisView.animate()
                    .translationY(0)
                    .setDuration(250)
                    .start();
        } else {
            //thisView.setTranslationY(0);
            thisView.animate()
                    .translationY(-height*3/4)
                    .setDuration(250)
                    .start();
        }

        isOpened = !isOpened;

    }
}
