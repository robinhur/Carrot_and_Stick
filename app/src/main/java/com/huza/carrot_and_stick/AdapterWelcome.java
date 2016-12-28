package com.huza.carrot_and_stick;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by HuZA on 2016-11-11.
 */

public class AdapterWelcome extends FragmentStatePagerAdapter {

    int[] welcome_images = {
            R.drawable.welcome_permission_1,
            R.drawable.welcome_permission_2,
            R.drawable.welcome_permission_3,
            R.drawable.welcome_permission_4,
            R.drawable.welcome_permission_5
    };

    public AdapterWelcome(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return welcome_images.length;
    }

    @Override
    public Fragment getItem(int position) {
        return WelcomePageFragment.create(welcome_images[position]);
    }
}
