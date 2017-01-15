package com.huza.carrot_and_stick;

import android.content.Context;
import android.content.Intent;
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

public class AdapterSetting extends BaseAdapter {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    ///////////////////////////////////////////////
    ///////////////////////////////////////////////
    ///////////////////////////////////////////////
    //// setting menu count ///////////////////////
    ///////////////////////////////////////////////
    int setting_count = 6; ////////////////////////
    ///////////////////////////////////////////////
    ////// 1. 알림영역 새로고침 | Switch | T //////
    ////// 2. 메인화면 자동복귀 | Switch | F //////
    ////// 3. 홈화면으로 이동   | Switch | F //////
    ////// 4. Credit 변화효과   | Switch | T //////
    ////// 5. 어플 완전 종료    | Button |   //●//
    ////// 6. 어플 삭제         | Button |   //●//
    ///////////////////////////////////////////////
    ///////////////////////////////////////////////
    ///////////////////////////////////////////////

    Context mContext;

    String[][] setting;

    public AdapterSetting(Context context) {
        mContext = context;

        setting = new String[setting_count][3];

        setting[0][0] = "알림영역 새로고침";
        setting[0][1] = "당근을 사용 시, 알림바를 매 초 갱신합니다.";
        setting[0][2] = "!Switch";

        setting[1][0] = "메인화면 자동복귀";
        setting[1][1] = "Credit에 변화가 있을 시, 자동으로 메인화면으로 돌아옵니다.";
        setting[1][2] = "Switch";

        setting[2][0] = "홈화면으로 이동";
        setting[2][1] = "잠금상태가 시작될 때, 홈화면으로 이동합니다.";
        setting[2][2] = "Switch";

        setting[3][0] = "Credit 변화효과";
        setting[3][1] = "Credit에 변화가 있을 시, 깜빡이는 효과를 적용합니다.";
        setting[3][2] = "!Switch";

        setting[4][0] = "어플리케이션 완전 종료";
        setting[4][1] = "어플리케이션을 로그아웃하고 종료합니다.";
        setting[4][2] = "Button";

        setting[5][0] = "어플리케이션 삭제";
        setting[5][1] = "어플리케이션을 기기에서 삭제합니다.";
        setting[5][2] = "Button";
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
        set_item_widget.removeAllViews();

        sett_item_big_text.setText(setting[i][0]);
        sett_item_small_text.setText(setting[i][1]);

        String widget_type = setting[i][2];
        Log.d(PACKAGE_NAME, "SettingAdapter : "+  i  +  " : " + widget_type);
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


    View.OnClickListener setting_btn_click = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            Log.d(PACKAGE_NAME, "SettingAdapter : OnClickListener | " + setting[view.getId()-1][0]);

            switch (setting[view.getId()-1][0]){
                case "어플리케이션 완전 종료":
                    //send_final_close_msg();

                    mContext.sendBroadcast(new Intent("com.huza.carrot_and_stick.finally_close"));
                    Toast.makeText(mContext, "어플리케이션 완전 종료", Toast.LENGTH_SHORT).show();
                    break;
            }

        }
    };
    CompoundButton.OnCheckedChangeListener setting_cb_click = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            Log.d(PACKAGE_NAME, "SettingAdapter : OnCheckedChangeListener | " + setting[compoundButton.getId()-1][0]);

            switch (setting[compoundButton.getId()-1][0]) {
                case "알림영역 새로고침":
                    break;
                case "메인화면 자동복귀":
                    break;
                case "홈화면으로 이동":
                    break;
                case "Credit 변화효과":
                    break;
            }

            if (b)
                setting[compoundButton.getId()-1][0] = "TRUE";
            else
                setting[compoundButton.getId()-1][1] = "FALSE";

        }
    };

}
