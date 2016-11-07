package com.huza.carrot_and_stick;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by HuZA on 2016-11-05.
 */

public class HistoryAdapter extends BaseAdapter {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    ArrayList<ArrayList<String>> history;
    Context mContext;

    SimpleDateFormat format_ymd = new SimpleDateFormat("yy년MM월dd일", Locale.KOREA);
    SimpleDateFormat format_hms = new SimpleDateFormat("aa hh:mm", Locale.KOREA);

    public HistoryAdapter(Context context) {
        mContext = context;
        history = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return history.size();
    }

    @Override
    public Object getItem(int i) {
        return history.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    private String getDate(String timestamp) {
        Calendar cal = Calendar.getInstance(Locale.KOREAN);
        cal.setTimeInMillis(Long.valueOf(timestamp)*1000);

        String date = format_ymd.format(cal.getTime());

        Calendar today = Calendar.getInstance(Locale.KOREAN);
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);

        //Log.d(PACKAGE_NAME, "HistoryAdapter : getDate1 : today : " + today.getTimeInMillis()/1000*1000);
        //Log.d(PACKAGE_NAME, "HistoryAdapter : getDate2 : stamp : " + Long.valueOf(timestamp)*1000);
        //Log.d(PACKAGE_NAME, "HistoryAdapter : getDate3 : diff  : " + (Long.valueOf(timestamp)*1000-today.getTimeInMillis()));

        if ((Long.valueOf(timestamp)*1000-today.getTimeInMillis()) > 0)
            date = "오늘";
        else if ((today.getTimeInMillis()-Long.valueOf(timestamp)*1000) < 86400000)
            date = "어제";

        return date;
    }
    private String getTime(String timestamp) {
        Calendar cal = Calendar.getInstance(Locale.KOREAN);
        cal.setTimeInMillis(Long.valueOf(timestamp)*1000);
        String time = format_hms.format(cal.getTime());

        //Log.d(PACKAGE_NAME, "HistoryAdapter : getTime1 : today : " + System.currentTimeMillis()/1000);
        //Log.d(PACKAGE_NAME, "HistoryAdapter : getTime2 : stamp : " + timestamp);
        //Log.d(PACKAGE_NAME, "HistoryAdapter : getTime3 : diff  : " + (System.currentTimeMillis()/1000-Long.valueOf(timestamp)));

        if ((System.currentTimeMillis()/1000-Long.valueOf(timestamp)) < 300)
            time = "방금";
        else if ((System.currentTimeMillis()/1000-Long.valueOf(timestamp)) < 3600)
            time = String.valueOf((System.currentTimeMillis()/1000-Long.valueOf(timestamp))/60)+"분 전";
        else if ((System.currentTimeMillis()/1000-Long.valueOf(timestamp)) < 18000)
            time = String.valueOf((System.currentTimeMillis()/1000-Long.valueOf(timestamp))/3600)+"시간 전";

        return time;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {

        Log.d(PACKAGE_NAME, "HistoryAdapter : getView called : " + i);

        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_history, viewGroup, false);
        }

        Log.d(PACKAGE_NAME, "HistoryAdapter : getView called history : " + history.get(i).get(0) + " : " + history.get(i).get(1)+history.get(i).get(2) + " : " + history.get(i).get(3));
        TextView tv_ymd = (TextView) view.findViewById(R.id.hist_item_ymd);
        TextView tv_hms = (TextView) view.findViewById(R.id.hist_item_hms);
        TextView tv_updown = (TextView) view.findViewById(R.id.hist_item_updown);
        TextView tv_content = (TextView) view.findViewById(R.id.hist_item_content);

        tv_ymd.setText(getDate(history.get(i).get(0)));
        tv_hms.setText(getTime(history.get(i).get(0)));
        tv_content.setText(history.get(i).get(3));

        tv_updown.setText(history.get(i).get(1)+history.get(i).get(2));
        if (history.get(i).get(1).equals("-"))
        {
            tv_updown.setTextColor(Color.BLUE);
        } else if (history.get(i).get(1).equals("+"))
        {
            tv_updown.setTextColor(Color.RED);
        }

        Log.d(PACKAGE_NAME, "HistoryAdapter : getView called textview : " + tv_ymd.getText() + " : " + tv_hms.getText() + " : " + tv_updown.getText() + " : " + tv_content.getText());

        return view;

    }

    public void reset_history() {
        history.clear();
    }

    public void addHistory(String timestamp, String updown, String delta, String content, int mode){

        Log.d(PACKAGE_NAME, "HistoryAdapter : addHistory called : " + delta);

        //mode : -1      앞에다가     넣기
        //mode : 기타    해당 index에 넣기

        ArrayList<String> item_history = new ArrayList<>();

        item_history.add(timestamp);
        item_history.add(updown);
        item_history.add(delta);
        item_history.add(content);

        if (mode == -1)
            history.add(item_history);
        else
            history.add(0,item_history);

        Log.d(PACKAGE_NAME, "HistoryAdapter : addHistory added : " + delta);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable(){
            @Override
            public void run(){
                Log.d(PACKAGE_NAME, "HistoryAdapter : addHistory : notifyDataSetChanged called (delayed)");
                notifyDataSetChanged();
            }
        }, 1000);

    }
}
