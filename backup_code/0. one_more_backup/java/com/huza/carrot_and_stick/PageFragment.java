package com.huza.carrot_and_stick;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Created by HuZA on 2016-10-26.
 */

public class PageFragment extends Fragment {

    int myPageNumber;

    public static PageFragment create(int PageNumber) {
        PageFragment fragment = new PageFragment();
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

        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.welcome_image, container, false);
        ImageView iv = (ImageView) rootView.findViewById(R.id.welcome_image_view);
        iv.setImageResource(myPageNumber);
        return rootView;

    }
}
