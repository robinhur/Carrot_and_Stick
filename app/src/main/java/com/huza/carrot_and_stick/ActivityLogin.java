package com.huza.carrot_and_stick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by HuZA on 2016-11-11.
 */

public class ActivityLogin extends AppCompatActivity {

    private static final String PACKAGE_NAME = "Carrot_and_Stick";

    EditText et_login_email;
    EditText et_login_password;
    ProgressBar progressBar;
    Button btn_login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        Log.d(PACKAGE_NAME, "ActivityLogin : 로그인 : " + pref.getBoolean("isLoggedin", false));
        if (pref.getBoolean("isLoggedin", false)) {

            Log.d(PACKAGE_NAME, "ActivityLogin : ServiceBackground 소환");
            sendBroadcast(new Intent("com.huza.carrot_and_stick.restartBACKGROUNDSERVICE"));
            finish();

        }

        et_login_email = (EditText) findViewById(R.id.et_login_email);
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        if (pref.getString("recent_user", null) != null) {
            et_login_email.setText(pref.getString("recent_user", null));
            et_login_password.requestFocus();
        }

        et_login_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if ((actionId == EditorInfo.IME_ACTION_DONE) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    log_in_clicked(null);
                    return true;
                }
                return false;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        btn_login = (Button) findViewById(R.id.btn_login_okay);

    }

    public void find_pw_clicked(View v) {
        ////// NIY ///////
    }

    public void sign_up_clicked(View v) {
        Intent i = new Intent(this, ActivitySignUp.class);
        startActivity(i);
    }

    public boolean signin_check_inputdata() {

        if (et_login_email.getText().toString().length() * et_login_password.getText().toString().length() == 0) {
            return false;
        }
        return true;

    }

    public void log_in_clicked(View v) {

        if (!signin_check_inputdata()) {
            Toast.makeText(ActivityLogin.this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        btn_login.setText("");
        btn_login.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(et_login_email.getText().toString(), et_login_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(ActivityLogin.this, "로그인 실패!!\nNIY!!!", Toast.LENGTH_SHORT).show();

                            btn_login.setText("로그인");
                            btn_login.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {

                            //// Login - SharegPreferences
                            SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);

                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("recent_user", et_login_email.getText().toString());
                            editor.putBoolean("isLoggedin", true);
                            editor.putString("user_uid", mAuth.getCurrentUser().getUid());
                            editor.commit();

                            Log.d(PACKAGE_NAME, "ActivityLogin : 로그인 : " + pref.getBoolean("isLoggedin", false));
                            //// Login - SharegPreferences

                            Log.d(PACKAGE_NAME, "ActivityLogin : ServiceBackground 소환");
                            sendBroadcast(new Intent("com.huza.carrot_and_stick.restartBACKGROUNDSERVICE"));

                            finish();
                        }

                    }
                });

    }
}