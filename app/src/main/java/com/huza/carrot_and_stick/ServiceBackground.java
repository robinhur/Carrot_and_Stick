package com.huza.carrot_and_stick;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

public class ServiceBackground extends Service {

    boolean lets_stop_thisloop = false; // for stop loop

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceAlwaysOnTop";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceCreditTicker";

    ReceiverStateListener statelistener;

    TelephonyManager manager;
    PhoneStateListener phoneStateListener;

    NotificationManager nm;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int user_credit;
    ArrayList<ArrayList<String>> history;
    //boolean history_initialized;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    ArrayList<ArrayList<String>> message_list;

    public ServiceBackground() {
        Log.d(PACKAGE_NAME, "ServiceBackground 생성");
    }

    @Override
    public void onDestroy() {
        Log.d(PACKAGE_NAME, "ServiceBackground 소멸!!!");
        unregisterReceiver(statelistener);
        super.onDestroy();
    }
    @Override
    public void onCreate() {
        super.onCreate();

        // init service!!!!! //
        if (statelistener == null) {
            statelistener = new ReceiverStateListener(this);

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
            intentFilter.setPriority(2147483647);

            registerReceiver(statelistener, intentFilter);

            Log.d(PACKAGE_NAME, "ServiceBackground : init : ReceiverStateListener 등록");
        }

        user_credit = -1;
        history = null;
        message_list = new ArrayList<>();

        pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        editor = pref.edit();

        Init_firebase();
        Init_phonestatelistener();
        Start_AoT();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Notification.Builder mBuilder = new Notification.Builder(getApplicationContext());

        mBuilder.setContentTitle("당근과 채찍 실행 중")
                .setContentText("")
                .setSmallIcon(R.drawable.carrot_noti);

        startForeground(0, mBuilder.build());
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotificationManager.cancel(0);

        Log.d(PACKAGE_NAME, "ServiceBackground : onStartCommand : startForeground 호출!!!");
        return super.onStartCommand(intent, flags, startId);

    }

    static final int PHONE_STATE_NOT_YET = -1;
    static final int PHONE_STATE_IDLE = 0;
    static final int PHONE_STATE_RINGING = 1;
    static final int PHONE_STATE_OUTGOING = 2;
    static final int PHONE_STATE_OFFHOOK = 3;

    int phone_state = PHONE_STATE_NOT_YET;
    String call_number = "";

    public void Init_phonestatelistener() {

        phoneStateListener = new PhoneStateListener() {
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {

                Log.d(PACKAGE_NAME, "ServiceBackground : phoneStateListener : onCallStateChanged : " + state);

                switch (state) {
                    case TelephonyManager.CALL_STATE_IDLE:
                        phone_state = PHONE_STATE_IDLE;
                        call_number = "";
                        break;
                    case TelephonyManager.CALL_STATE_RINGING:
                        phone_state = PHONE_STATE_RINGING;
                        call_number = incomingNumber;
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        if (phone_state != PHONE_STATE_OUTGOING)
                            phone_state = PHONE_STATE_OFFHOOK;
                        break;
                }

                sendPhoneState();

            }
        };

        manager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        manager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);

    }
    public void Init_firebase() {
        Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : started");
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);

        ////////user credit////////
        databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (user_credit == -1) {
                    Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : user_credit init!!! : " + dataSnapshot.getValue().toString());
                    user_credit = Integer.valueOf(dataSnapshot.getValue().toString());
                    check_settle_up();
                } else {
                    Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : user_credit changed!!: " + dataSnapshot.getValue().toString());
                    user_credit = Integer.valueOf(dataSnapshot.getValue().toString());
                    if (checkServiceRunning(AoT_SERVICE_NAME))
                        sendUserCredit();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ///////////////////////////

        ////////log history////////
        history = new ArrayList<>();

        Calendar date = Calendar.getInstance();
        Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_init : " + date.getTimeInMillis());
        date.add(Calendar.DATE,-6);
        date.set(Calendar.HOUR, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_init : " + date.getTimeInMillis());

        //databaseReference.child("logs").child(pref.getString("user_uid", null)).orderByKey().endAt("9999999999").limitToLast(100).addListenerForSingleValueEvent(new ValueEventListener() {
        databaseReference.child("logs").child(pref.getString("user_uid", null)).orderByKey().startAt(String.valueOf(date.getTimeInMillis()/1000)).endAt("9999999999").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {

                        Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_init : size = " + dataSnapshot.getChildrenCount());

                        Iterator<DataSnapshot> it = dataSnapshot.getChildren().iterator();

                        while (it.hasNext()) {
                            DataSnapshot now_temp = it.next();

                            ArrayList<String> item_history = new ArrayList<String>();

                            item_history.add(now_temp.getKey());
                            item_history.add(now_temp.child("updown").getValue().toString());
                            item_history.add(now_temp.child("delta").getValue().toString());
                            item_history.add(now_temp.child("content").getValue().toString());

                            history.add(0, item_history);
                        }

                        /*Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_init : HistoryAdapter : ----------------------------");
                        Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_init : HistoryAdapter : " + String.valueOf(history.size()));
                        for (int i = 0; i < history.size(); i++) {
                            Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_init : HistoryAdapter : item : " + i + " : " + history.get(i).toString());

                        }
                        Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_init : HistoryAdapter : ----------------------------");*/

                    }

                    //sendHistory();
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        databaseReference.child("logs").child(pref.getString("user_uid", null)).limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                //if (history_initialized) {
                if (history.size() != 0) {
                    Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_added : HistoryAdapter : ----------------------------");
                    Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_added : " + dataSnapshot.getKey() + "|" + dataSnapshot.child("updown").getValue().toString() + "|" +
                            dataSnapshot.child("delta").getValue().toString() + "|" + dataSnapshot.child("content").getValue().toString());
                    Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : log_added : HistoryAdapter : ----------------------------");

                    ArrayList<String> item_history = new ArrayList<String>();

                    item_history.add(dataSnapshot.getKey());
                    item_history.add(dataSnapshot.child("updown").getValue().toString());
                    item_history.add(dataSnapshot.child("delta").getValue().toString());
                    item_history.add(dataSnapshot.child("content").getValue().toString());

                    history.add(0, item_history);
                    history.remove(history.size()-1);

                    if (checkServiceRunning(AoT_SERVICE_NAME))
                        sendHistory();
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        ///////////////////////////
    }

    public void check_settle_up() {

        if (pref.getInt("second", -1) != -1) {
            Log.d(PACKAGE_NAME, "ServiceBackground : check_settle_up | found | " + pref.getInt("second", -1));

            databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").setValue(user_credit - pref.getInt("second", -1));

            /////////////// Log ///////////////
            DataLog log = new DataLog(System.currentTimeMillis()/1000, "-", pref.getInt("second", -1), "비정상 정산");
            databaseReference.child("logs").child(pref.getString("user_uid", null)).child(String.valueOf(log.getTimestamp())).setValue(log);
            //////////////////////////////////

            editor.remove("second");
            editor.commit();
        } else {
            Log.d(PACKAGE_NAME, "ServiceBackground : check_settle_up | not-found");

        }

    }
    public void sendUserCredit() {

        Log.d(PACKAGE_NAME, "ServiceBackground : sendUserCredit : " + user_credit);

        if (user_credit != -1)
            sendMessage(102, String.valueOf(user_credit));
        else
            sendMessage(999, "102");

    }
    public void changeUserCredit(int delta) {

        Log.d(PACKAGE_NAME, "ServiceBackground : changeUserCredit : " + user_credit + " : " + delta);

        if (user_credit - delta < 0)
            delta = user_credit;

        databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").setValue(user_credit - delta);

        /////////////// Log ///////////////
        DataLog log = new DataLog(System.currentTimeMillis()/1000, "-", delta, "정산");
        databaseReference.child("logs").child(pref.getString("user_uid", null)).child(String.valueOf(log.getTimestamp())).setValue(log);
        //////////////////////////////////

    }

    public void sendPhoneState() {

        Log.d(PACKAGE_NAME, "ServiceBackground : sendUserCredit : " + user_credit);

        if (phone_state == PHONE_STATE_NOT_YET) {
            sendMessage(999, "151");
        } else {
            sendMessage(151, String.valueOf(phone_state));
        }

    }
    public void sendOutgoingcall(String outgoing_number) {
        editor.putString("outgoing_NUMBER", outgoing_number);
        editor.commit();

        Log.d(PACKAGE_NAME, "ServiceBackground : sendOutgoingcall | outgoing_NUMBER = " + outgoing_number);

        if (checkServiceRunning(AoT_SERVICE_NAME)){
            sendMessage(151, outgoing_number);
        } else if (checkServiceRunning(CreditTicker_SERVICE_NAME)) {
            sendMessage(552, outgoing_number);
        }
    }
    public void sendHistory() {

        Log.d(PACKAGE_NAME, "ServiceBackground : sendHistory : " + user_credit);

        if (history.size() != 0)
            sendMessage(104, "!");
        else
            sendMessage(999, "104");

    }

    public void Start_AoT() {
        //// 혹시 몰라 noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////
        if (checkServiceRunning(AoT_SERVICE_NAME)) return;

        lets_stop_thisloop = false;

        Log.d(PACKAGE_NAME, "ServiceBackground : Start_AoT : AoT 서비스를 시작합니다 : " + user_credit);
        startService(new Intent(this, ServiceAlwaysOnTop.class));
    }
    public void Start_CreditTicker() {
        if (checkServiceRunning(CreditTicker_SERVICE_NAME)) return;

        lets_stop_thisloop = false;

        Intent i = new Intent(this, ServiceCreditTicker.class);
        i.putExtra("user_credit", user_credit);
        startService(i);

        sendMessage(500, null);
    }

    public void connect_with_AoT() {
        bindService(new Intent(getApplicationContext(), ServiceAlwaysOnTop.class), mConnection_AoT, Context.BIND_AUTO_CREATE);

        sendMessage(100, null);
        //if (history_initialized) sendHistory();
        //sendUserCredit();
    }

    public void Close_AoT() {
        sendMessage(198, null);
    }
    public void Close_CreditTicker() {
        sendMessage(598, null);
    }

    public void shutdown_AoT(){
        Log.d(PACKAGE_NAME, "ServiceBackground : shutdown_AoT : AoT 서비스를 종료 합니다");
        stopService(new Intent(this, ServiceAlwaysOnTop.class));
    }
    public void shutdown_CT() {
        Log.d(PACKAGE_NAME, "ServiceBackground : shutdown_CT : CT 서비스를 종료 합니다");
        stopService(new Intent(this, ServiceCreditTicker.class));
        //// 혹시 몰라 noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////
    }




    ////////////////////////////////////////////////////////////////////////////////////ver.170115//
    /////        ////          ////   //////  ////       ///////////// Always On Top ///////////////
    ///  ////////////  ////////////  /  ////  ////  ////  /////////// Connect w/AoT      : 100 /////
    ////        /////          ////  ///  //  ////  /////  ////////// send Credit        : 102 /////
    //////////   ////  ////////////  /////    ////  ////  /////////// send Setting       : 103 /////
    ///        //////          ////  ///////  ////       //////////// send History       : 104 /////
    ///////////////////////////////////////////////////////////////// request PHONESTATE : 151 /////
    ///////////////////////////////////////////////////////////////// Disconnect w/AoT   : 198 /////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////// Credit Ticker ///////////////
    ///////////////////////////////////////////////////////////////// Connect w/CT       : 500 /////
    ///////////////////////////////////////////////////////////////// send Credit        : 502 /////
    ///////////////////////////////////////////////////////////////// alert OutgoingCall : 552 /////
    ///////////////////////////////////////////////////////////////// Disconnect w/CT    : 598 /////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void add_to_message_queue(int what, String extra_data) {
        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : add_to_message_queue!! : " + what + " : " + extra_data);

        ArrayList<String> message = new ArrayList<>();
        message.add(String.valueOf(what));
        message.add(extra_data);

        message_list.add(message);

        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : add_to_message_queue!! : " + message_list.size());
    }
    public synchronized void sender_unit(int mode, Message msg) {
        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : sender_unit started " + mode + " : " + msg.what + " | " + msg.getData());

        switch(mode) {
            case 0:

                try {
                    mService_AoT.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
            case 1:

                try {
                    mService_Ticker.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                break;
        }
    }
    public void sendMessage(int what, String extra_data) {

        if (what == 0) return;

        if (what/499 == 0 || ((what == 999)&&(extra_data.substring(0, 1).equals("1")))) {

            Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : sendMessage0 = " + mBound_AoT + " : " + what + " | " + extra_data + " : lets_stop_thisloop = " + lets_stop_thisloop);
            if (!mBound_AoT){
                if (what != -1)
                    add_to_message_queue(what, extra_data);

                bindService(new Intent(getApplicationContext(), ServiceAlwaysOnTop.class), mConnection_AoT, Context.BIND_AUTO_CREATE);
            }
            else {
                Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : true0 : " + message_list.size());
                while(message_list.size() != 0) {
                    Message msg = Message.obtain(null, Integer.valueOf(message_list.get(0).get(0)) , 0, 0);

                    Bundle data = new Bundle();
                    if (message_list.get(0).get(1) != null) {

                        if (Integer.valueOf(message_list.get(0).get(0)) == 104){
                            Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : add history : " + history.size() + " | " + history.get(0));
                            data.putSerializable("log_init", history);
                        }
                        else
                            data.putString("extra_data" , message_list.get(0).get(1));

                    }
                    if (Integer.valueOf(message_list.get(0).get(0)) == 151){
                        data.putString("extra_data", String.valueOf(phone_state));
                        data.putString("call_number", call_number);
                    }

                    msg.setData(data);

                    if (Integer.valueOf(message_list.get(0).get(0))/499 ==0 || Integer.valueOf(message_list.get(0).get(0)) == 999)
                        sender_unit(0, msg);
                    else
                        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : MessageQueue Error!!!! what = " + Integer.valueOf(message_list.get(0).get(0)));

                    message_list.remove(0);
                }

                Message msg = Message.obtain(null, what, 0, 0);

                Bundle data = new Bundle();
                if (extra_data != null) {

                    if (what == 104)
                        data.putSerializable("log_init", history);
                    else
                        data.putString("extra_data" , extra_data);
                }
                if (what == 151){
                    data.putString("extra_data" , String.valueOf(phone_state));
                    data.putString("call_number", call_number);
                }

                msg.setData(data);

                sender_unit(0, msg);

                if (what == 198) {
                    unbindService(mConnection_AoT);
                    mConnection_AoT.onServiceDisconnected(null);

                    if (!lets_stop_thisloop)
                        Start_CreditTicker();

                    lets_stop_thisloop = false;
                }
                /*else if (what == 104) {
                    history_initialized = true;
                }*/
            }

        } else if (what/499 == 1|| ((what == 999)&&(extra_data.substring(0, 1).equals("5")))) {

            Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : sendMessage1 = " + mBound_Ticker + " : " + what + " : lets_stop_thisloop = " + lets_stop_thisloop);
            if (!mBound_Ticker){
                if ((what-500) != -1)
                    add_to_message_queue(what, extra_data);

                bindService(new Intent(getApplicationContext(), ServiceCreditTicker.class), mConnection_Ticker, Context.BIND_AUTO_CREATE);
            }
            else {
                Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : true1 : " + message_list.size());
                while (message_list.size() != 0) {
                    Message msg = Message.obtain(null, Integer.valueOf(message_list.get(0).get(0)) , 0, 0);

                    if (message_list.get(0).get(1) != null) {
                        Bundle data = new Bundle();
                        data.putString("extra_data" , extra_data);
                        msg.setData(data);
                    }

                    if (Integer.valueOf(message_list.get(0).get(0))/499 ==1 || Integer.valueOf(message_list.get(0).get(0)) == 999)
                        sender_unit(1, msg);
                    else
                        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : MessageQueue Error!!!! what = " + Integer.valueOf(message_list.get(0).get(0)));

                    message_list.remove(0);
                }

                Message msg = Message.obtain(null, what, 0, 0);

                if (extra_data != null) {
                    Bundle data = new Bundle();
                    data.putString("extra_data" , extra_data);
                    msg.setData(data);
                }

                sender_unit(1, msg);

                if (what == 598) {
                    unbindService(mConnection_Ticker);
                    mConnection_Ticker.onServiceDisconnected(null);

                    if (!lets_stop_thisloop)
                        Start_AoT();

                    lets_stop_thisloop = false;
                }

            }
        } else {
            Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : sendMessage : message error!!!! : " + what + " : " + extra_data);
        }
    }


    ////////////////////////ver.170115//
    ///// AoT gogogogo       : 1   /////
    ///// NEW OUTGOING CALL  : 2   /////
    ///// CreditTicker close : 5   /////
    ///// Finally Close      : 99  /////
    ////////////////////////////////////
    ///// AoT connected      : 101 /////
    ///// AoT req CREDIT     : 102 /////
    ///// AoT req SETTING    : 103 /////
    ///// AoT req LOG info   : 104 /////
    ///// AoT req PHONESTATE : 151 /////
    ///// AoT diconn req w/o : 196 /////
    ///// AoT diconn req     : 197 /////
    ///// AoT end msg        : 199 /////
    ////////////////////////////////////
    ///// CT connected       : 501 /////
    ///// CT diconn req w/o  : 596 /////
    ///// CT diconn req      : 597 /////
    ///// CT end msg + time  : 599 /////
    ////////////////////////////////////
    ///// FAIL!! resend code : 999 /////
    ////////////////////////////////////
    @Override
    public IBinder onBind(Intent intent) {
        return BackgroundMessenger.getBinder();
    }
    final Messenger BackgroundMessenger = new Messenger(new BackgroundIncomingHandler());
    class BackgroundIncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : BackgroundIncomingHandler = " + msg.what);

            switch (msg.what) {
                case 1:
                    Start_AoT();
                    break;
                case 2:
                    Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : BackgroundIncomingHandler : " + msg.getData().getString("extra_data"));
                    phone_state = PHONE_STATE_OUTGOING;
                    call_number = msg.getData().getString("extra_data");
                    sendPhoneState();

                    //sendOutgoingcall(msg.getData().getString("extra_data"));
                    break;
                case 5:
                    if (checkServiceRunning(CreditTicker_SERVICE_NAME)) {
                        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : BackgroundIncomingHandler = checkServiceRunning(CreditTicker_SERVICE_NAME) | true");
                        lets_stop_thisloop = true;
                        Close_CreditTicker();
                    } else {
                        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : BackgroundIncomingHandler = checkServiceRunning(CreditTicker_SERVICE_NAME) | false");
                    }
                    break;
                case 99:
                    if (checkServiceRunning(CreditTicker_SERVICE_NAME)){
                        lets_stop_thisloop = true;
                        Close_CreditTicker();
                    }
                    if (checkServiceRunning(AoT_SERVICE_NAME)){
                        lets_stop_thisloop = true;
                        Close_AoT();
                    }
                    stopSelf();
                    break;


                case 101:
                    Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : BackgroundIncomingHandler : AoT connected");
                    connect_with_AoT();
                    break;
                case 102:
                    sendUserCredit();
                    break;
                case 103:
                    break;
                case 104:
                    sendHistory();
                    break;
                case 151:
                    sendPhoneState();
                    break;
                case 196:
                    lets_stop_thisloop = true;
                case 197:
                    Close_AoT();
                    break;
                case 199:
                    Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : AoT 죽여?");
                    shutdown_AoT();
                    break;


                case 501:
                    Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : BackgroundIncomingHandler : Ticker connected");
                    bindService(new Intent(getApplicationContext(), ServiceCreditTicker.class), mConnection_Ticker, Context.BIND_AUTO_CREATE);
                    break;
                case 596:
                    lets_stop_thisloop = true;
                case 597:
                    Close_CreditTicker();
                    break;
                case 599:
                    Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : CT 죽여? : second : " + msg.getData().getString("extra_data"));
                    changeUserCredit(Integer.valueOf(msg.getData().getString("extra_data")));
                    shutdown_CT();
                    //// 혹시 몰라 noti 지우기 ////
                    nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.cancel(737);
                    ///////////////////////////////
                    break;


                case 999:
                    ServiceBackground.this.sendMessage(Integer.valueOf(msg.getData().getString("extra_data")), null);
            }

        }
    }

    public boolean checkServiceRunning(String service_name) {

        ActivityManager serviceChecker = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo runningServiceInfo:serviceChecker.getRunningServices(Integer.MAX_VALUE)) {
            if (service_name.equals(runningServiceInfo.service.getClassName())){
                Log.d(PACKAGE_NAME, "ServiceBackground : checkServiceRunning : "+ service_name + " = found!!!!");
                return true;
            }
        }

        Log.d(PACKAGE_NAME, "ServiceBackground : checkServiceRunning : "+ service_name + " = not running");
        return false;

    }

    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    //Service Connection//
    Messenger mService_AoT = null;
    boolean mBound_AoT;
    private ServiceConnection mConnection_AoT = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.d(PACKAGE_NAME, "ServiceBackground : mConnection_AoT : connected");
            mService_AoT = new Messenger(iBinder);
            mBound_AoT = true;
            mBound_Ticker = false;
            ///// for reconnect /////
            sendMessage(-1, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(PACKAGE_NAME, "ServiceBackground : mConnection_AoT : disconnected");

            mService_AoT = null;
            mBound_AoT = false;
        }
    };

    Messenger mService_Ticker = null;
    boolean mBound_Ticker;
    private ServiceConnection mConnection_Ticker = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_Ticker = new Messenger(iBinder);
            mBound_Ticker = true;
            mBound_AoT = false;
            ///// for reconnect /////
            sendMessage(499, null);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(PACKAGE_NAME, "ServiceBackground : mConnection_Ticker : disconnected");

            mService_Ticker = null;
            mBound_Ticker = false;
        }
    };
}
