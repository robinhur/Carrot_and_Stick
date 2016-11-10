package com.huza.carrot_and_stick;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by HuZA on 2016-10-26.
 */

public class WelcomeAdapter extends FragmentStatePagerAdapter {

    int[] welcome_images = {
            R.drawable.welcome_permission_1,
            R.drawable.welcome_permission_2,
            R.drawable.welcome_permission_3,
            R.drawable.welcome_permission_4
    };

    public WelcomeAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return welcome_images.length;
    }

    @Override
    public Fragment getItem(int position) {
        return PageFragment.create(welcome_images[position]);
    }

}
