package com.huza.carrot_and_stick;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by HuZA on 2016-11-05.
 */

public class SettingAdapter extends BaseAdapter {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    String[][] setting;
    int setting_count = 4;

    Context mContext;

    View.OnClickListener setting_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()-1){
                case 3:
                    send_final_close_msg();
                    Toast.makeText(mContext, "어플리케이션 완전 종료", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };
    CompoundButton.OnCheckedChangeListener setting_cb_click = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            if (b)
                Toast.makeText(mContext, setting[compoundButton.getId()-1][0] + " : true", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(mContext, setting[compoundButton.getId()-1][0] + " : false", Toast.LENGTH_SHORT).show();

            switch (compoundButton.getId()-1) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        }
    };

    Messenger mService_AoT = null;
    Boolean mBound_AoT = false;
    ServiceConnection mConnection_AoT = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_AoT = new Messenger(iBinder);
            mBound_AoT = true;
            Log.d(PACKAGE_NAME, "SettingAdapter : onServiceConnected");
            send_final_close_msg();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mService_AoT = null;
            mBound_AoT = false;
            Log.d(PACKAGE_NAME, "SettingAdapter : onServiceConnected");
        }
    };

    private void send_final_close_msg() {
        Log.d(PACKAGE_NAME, "SettingAdapter : send_final_close_msg() : " + mBound_AoT);

        if (!mBound_AoT){
            mContext.bindService(new Intent(mContext, AlwaysOnTop.class), mConnection_AoT, Context.BIND_AUTO_CREATE);
            Log.d(PACKAGE_NAME, "SettingAdapter : send_final_close_msg() : bindService");
            return;
        }

        Message msg = Message.obtain(null, 2, 0, 0);
        try {
            Log.d(PACKAGE_NAME, "SettingAdapter : send_final_close_msg() : message sent");
            mService_AoT.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        mContext.unbindService(mConnection_AoT);
        Log.d(PACKAGE_NAME, "SettingAdapter : send_final_close_msg() : unbindService");
        mConnection_AoT.onServiceDisconnected(null);
    }


    public SettingAdapter(Context context) {
        mContext = context;

        setting = new String[setting_count][3];

        setting[0][0] = "광고영역 표시";
        setting[0][1] = "잠금상태에서 하단 광고영역을 숨깁니다.";
        setting[0][2] = "!Switch";

        setting[1][0] = "네비게이션바 표시";
        setting[1][1] = "잠금상태에서 하단 네비게이션바를 표시합니다.";
        setting[1][2] = "Switch";

        setting[2][0] = "알림영역 새로고침";
        setting[2][1] = "당근을 사용 시, 알림바를 매 초 갱신합니다.";
        setting[2][2] = "!Switch";

        setting[3][0] = "어플리케이션 완전 종료";
        setting[3][1] = "어플리케이션을 로그아웃하고 종료합니다.";
        setting[3][2] = "Button";
    }

    @Override
    public int getCount() {
        return setting.length;
    }

    @Override
    public Object getItem(int i) {
        return setting[i];
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

        TextView sett_item_big_text = (TextView) view.findViewById(R.id.sett_item_big_text);
        TextView sett_item_small_text = (TextView) view.findViewById(R.id.sett_item_small_text);
        LinearLayout set_item_widget = (LinearLayout) view.findViewById(R.id.set_item_widget);

        sett_item_big_text.setText(setting[i][0]);
        sett_item_small_text.setText(setting[i][1]);

        String widget_type = setting[i][2];
        Boolean widget_selected = false;
        if (widget_type.substring(0,1).equals("!")) {
            widget_type = widget_type.substring(1,widget_type.length());
            widget_selected = true;
        }

        switch (widget_type) {
            case "Button":
                Button btn = new Button(mContext);
                btn.setId(i+1);
                btn.setText("CLICK");
                btn.setTextSize((float)10.0);
                btn.setPadding(0,0,0,0);
                btn.setGravity(Gravity.CENTER);
                btn.setLayoutParams (new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                btn.setOnClickListener(setting_btn_click);
                set_item_widget.addView(btn);
                break;
            case "Switch":
                Switch cb = new Switch(mContext);
                cb.setId(i+1);
                cb.setPadding(0,0,0,0);
                cb.setGravity(Gravity.CENTER);
                cb.setLayoutParams (new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                cb.setOnCheckedChangeListener(setting_cb_click);
                cb.setChecked(widget_selected);
                set_item_widget.addView(cb);
                break;
        }

        Log.d(PACKAGE_NAME, "SettingAdapter : initialized item : " + i);

        return view;

    }

}
