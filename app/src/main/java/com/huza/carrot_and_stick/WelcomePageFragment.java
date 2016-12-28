package com.huza.carrot_and_stick;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by HuZA on 2016-11-11.
 */

public class WelcomePageFragment extends Fragment {

    int myPageNumber;

    public static WelcomePageFragment create(int PageNumber) {
        WelcomePageFragment fragment = new WelcomePageFragment();
        Bundle args = new Bundle();
        args.putInt("page", PageNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myPageNumber = getArguments().getInt("page");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.layout_welcome_image, container, false);
        ImageView iv = (ImageView) rootView.findViewById(R.id.welcome_image_view);
        iv.setImageResource(myPageNumber);
        return rootView;

    }
}