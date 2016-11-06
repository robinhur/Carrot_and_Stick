package com.huza.carrot_and_stick;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;

/**
 * Created by HuZA on 2016-11-05.
 */

public class SettingAdapter extends BaseAdapter {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    ArrayList<ArrayList<String>> setting;
    Context mContext;

    public SettingAdapter(Context context) {
        mContext = context;
        setting = new ArrayList<>();

        ArrayList<String> item_history = new ArrayList<>();

        item_history.add("asdf");
        item_history.add("Asdf");
        item_history.add("Asdf");

        setting.add(item_history);

    }

    @Override
    public int getCount() {
        return setting.size();
    }

    @Override
    public Object getItem(int i) {
        return setting.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_setting, viewGroup, false);
        }

        return view;

    }

}
