package com.huza.carrot_and_stick;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    EditText et_login_email;
    EditText et_login_password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);
        Log.d(PACKAGE_NAME, "LoginActivity : 로그인 : "+ pref.getBoolean("isLoggedin", false));
        if (pref.getBoolean("isLoggedin", false)) {

            Log.d(PACKAGE_NAME, "LoginActivity : BackgroundService 소환");
            sendBroadcast(new Intent("com.huza.carrot_and_stick.restartBACKGROUNDSERVICE"));

            Log.d(PACKAGE_NAME, "LoginActivity : AoT 소환");
            sendBroadcast(new Intent("com.huza.carrot_and_stick.restartAoTSERVICE"));

            finish();

        }

        et_login_email = (EditText) findViewById(R.id.et_login_email);
        et_login_password = (EditText) findViewById(R.id.et_login_password);
        if (pref.getString("recent_user", null) != null)
            et_login_email.setText(pref.getString("recent_user",null));

        et_login_password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    log_in_clicked(null);
                    return true;
                }
                return false;
            }
        });

    }

    public void find_pw_clicked(View v) {
    }

    public void sign_up_clicked(View v) {
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }

    public boolean signin_check_inputdata() {

        if (et_login_email.getText().toString().length()*et_login_password.getText().toString().length() == 0){
            return false;
        }
        return true;

    }

    public void log_in_clicked(View v) {

        if (!signin_check_inputdata()){
            Toast.makeText(LoginActivity.this, "아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.signInWithEmailAndPassword(et_login_email.getText().toString(), et_login_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {

                        if (!task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "로그인 실패!!\nNIY!!!", Toast.LENGTH_SHORT).show();
                        } else {

                            //// Login - SharegPreferences
                            SharedPreferences pref = getSharedPreferences("Carrot_and_Stick", MODE_PRIVATE);

                            SharedPreferences.Editor editor = pref.edit();
                            editor.putString("recent_user", et_login_email.getText().toString());
                            editor.putBoolean("isLoggedin", true);
                            editor.putString("user_uid", mAuth.getCurrentUser().getUid());
                            editor.commit();

                            Log.d(PACKAGE_NAME, "LoginActivity : 로그인 : "+ pref.getBoolean("isLoggedin", false));
                            //// Login - SharegPreferences

                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(i);
                            finish();
                            
                        }
                        
                    }
                });

    }
}
