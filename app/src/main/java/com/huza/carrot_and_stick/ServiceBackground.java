package com.huza.carrot_and_stick;

import android.app.ActivityManager;
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
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ServiceBackground extends Service {

    boolean lets_stop_thisloop = false; // for stop loop

    final String PACKAGE_NAME = "Carrot_and_Stick";
    final String AoT_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceAlwaysOnTop";
    final String CreditTicker_SERVICE_NAME = "com.huza.carrot_and_stick.ServiceCreditTicker";

    ReceiverStateListener statelistener = null;
    NotificationManager nm;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    int user_credit;

    int what;
    String extra_data;

    SharedPreferences pref;
    SharedPreferences.Editor editor;

    public ServiceBackground() {
        Log.d(PACKAGE_NAME, "ServiceBackground 생성");
    }

    @Override
    public void onDestroy() {
        Log.d(PACKAGE_NAME, "ServiceBackground 소멸!!!");
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

        what = 0;
        extra_data = null;
        user_credit = -1;

        pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        editor = pref.edit();

        Init_firebase();
        Start_AoT();
    }

    public void Init_firebase() {
        Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : started");
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (user_credit == -1) {
                    Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : user_credit init!!! : " + dataSnapshot.getValue().toString());
                    check_settle_up();
                } else {
                    Log.d(PACKAGE_NAME, "ServiceBackground : Init_firebase : user_credit changed!!: " + dataSnapshot.getValue().toString());
                }

                user_credit = Integer.valueOf(dataSnapshot.getValue().toString());

                if (checkServiceRunning(AoT_SERVICE_NAME))
                    sendUserCredit();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void check_settle_up() {
        Log.d(PACKAGE_NAME, "ServiceBackground : check_settle_up");
    }
    public void sendUserCredit() {

        if (user_credit == -1) return;

        what = 102;
        extra_data = String.valueOf(user_credit);
        sendMessage();

    }
    public void changeUserCredit(int delta) {

        Log.d(PACKAGE_NAME, "ServiceBackground : changeUserCredit : " + user_credit + " : " + delta);

        if (user_credit - delta < 0)
            delta = user_credit;

        databaseReference.child("users").child(pref.getString("user_uid", null)).child("credit").setValue(user_credit - delta);

    }
z
    public void Start_AoT() {
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

        what = 500;
        sendMessage();
    }

    public void connect_with_AoT() {
        bindService(new Intent(getApplicationContext(), ServiceAlwaysOnTop.class), mConnection_AoT, Context.BIND_AUTO_CREATE);
        what=100;
        sendMessage();

        sendUserCredit();
    }

    public void Close_AoT() {
        what = 198;
        sendMessage();
    }
    public void Close_CreditTicker() {
        what = 598;
        sendMessage();
    }

    public void shutdown_AoT(){
        stopService(new Intent(this, ServiceAlwaysOnTop.class));
    }
    public void shutdown_CT() {
        stopService(new Intent(this, ServiceCreditTicker.class));
        //// 혹시 몰라 noti 지우기 ////
        nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(737);
        ///////////////////////////////
    }


    ////////////////////////////////////////////////////////////////////////////////////ver.161228//
    /////        ////          ////   //////  ////       ///////////// Always On Top ///////////////
    ///  ////////////  ////////////  /  ////  ////  ////  /////////// Connect w/AoT      : 100 /////
    ////        /////          ////  ///  //  ////  /////  ////////// send Credit        : 102 /////
    //////////   ////  ////////////  /////    ////  ////  /////////// send Setting       : 103 /////
    ///        //////          ////  ///////  ////       //////////// alert OutgoingCall : 152 /////
    ///////////////////////////////////////////////////////////////// Disconnect w/AoT   : 198 /////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////// Credit Ticker ///////////////
    ///////////////////////////////////////////////////////////////// Connect w/CT       : 500 /////
    ///////////////////////////////////////////////////////////////// send Credit        : 502 /////
    ///////////////////////////////////////////////////////////////// alert OffHook      : 553 /////
    ///////////////////////////////////////////////////////////////// Disconnect w/CT    : 598 /////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    public void sendMessage() {

        if (what == 0) return;

        Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : sendMessage : message = " + what);

        if (what/500 == 0) {
            if (!mBound_AoT)
                bindService(new Intent(getApplicationContext(), ServiceAlwaysOnTop.class), mConnection_AoT, Context.BIND_AUTO_CREATE);
            else {
                Message msg = Message.obtain(null, what, 0, 0);

                if (extra_data != null) {
                    Bundle data = new Bundle();
                    data.putString("extra_data" , extra_data);
                    msg.setData(data);
                }

                try {
                    mService_AoT.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (what == 198) {
                    unbindService(mConnection_AoT);
                    mConnection_AoT.onServiceDisconnected(null);

                    if (!lets_stop_thisloop)
                        Start_CreditTicker();

                    lets_stop_thisloop = false;
                }

                what = 0;
                extra_data = null;
            }
        } else if (what/500 == 1) {
            if (!mBound_Ticker)
                bindService(new Intent(getApplicationContext(), ServiceCreditTicker.class), mConnection_Ticker, Context.BIND_AUTO_CREATE);
            else {
                Message msg = Message.obtain(null, what, 0, 0);

                if (extra_data != null) {
                    Bundle data = new Bundle();
                    data.putString("extra_data" , extra_data);
                    msg.setData(data);
                }

                try {
                    mService_Ticker.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

                if (what == 598) {
                    unbindService(mConnection_Ticker);
                    mConnection_Ticker.onServiceDisconnected(null);

                    if (!lets_stop_thisloop)
                        Start_AoT();

                    lets_stop_thisloop = false;
                }

                what = 0;
                extra_data = null;
            }
        } else {
            Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : sendMessage : message error!!!! : " + what + " : " + extra_data);
        }
    }


    ////////////////////////ver.161228//
    ///// AoT gogogogo       : 1   /////
    ///// NEW OUTGOING CALL  : 2   /////
    ///// CreditTicker close : 5   /////
    ///// Finally Close      : 99  /////
    ////////////////////////////////////
    ///// AoT connected      : 101 /////
    ///// AoT diconn req w/o : 196 /////
    ///// AoT diconn req     : 197 /////
    ///// AoT end msg        : 199 /////
    ////////////////////////////////////
    ///// CT connected       : 501 /////
    ///// CT diconn req w/o  : 596 /////
    ///// CT diconn req      : 597 /////
    ///// CT end msg + time  : 599 /////
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
                    break;
                case 5:
                    if (checkServiceRunning(CreditTicker_SERVICE_NAME)) {
                        lets_stop_thisloop = true;
                        Close_CreditTicker();
                    }
                    break;
                case 99:
                    break;


                case 101:
                    Log.d(PACKAGE_NAME, "ServiceBackground : MESSAGE : BackgroundIncomingHandler : AoT connected");
                    connect_with_AoT();
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
                    break;
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
    Messenger mService_Ticker = null;
    boolean mBound_Ticker;
    private ServiceConnection mConnection_Ticker = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_Ticker = new Messenger(iBinder);
            mBound_Ticker = true;
            mBound_AoT = false;
            sendMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(PACKAGE_NAME, "ServiceBackground : mConnection_Ticker : disconnected");

            mService_Ticker = null;
            mBound_Ticker = false;
        }
    };

    Messenger mService_AoT = null;
    boolean mBound_AoT;
    private ServiceConnection mConnection_AoT = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            mService_AoT = new Messenger(iBinder);
            mBound_AoT = true;
            mBound_Ticker = false;
            sendMessage();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.d(PACKAGE_NAME, "ServiceBackground : mConnection_AoT : disconnected");

            mService_AoT = null;
            mBound_AoT = false;
        }
    };
}
