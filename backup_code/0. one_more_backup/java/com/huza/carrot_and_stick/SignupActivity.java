package com.huza.carrot_and_stick;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity {

    final String PACKAGE_NAME = "Carrot_and_Stick";

    EditText et_signup_email;
    EditText et_signup_password;
    EditText et_signup_name;

    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ProgressBar progressBar;
    Button btn_signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE|WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);

        et_signup_email = (EditText) findViewById(R.id.et_signup_email);
        et_signup_password = (EditText) findViewById(R.id.et_signup_password);
        et_signup_name = (EditText) findViewById(R.id.et_signup_name);
        
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        et_signup_name.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                    register_clicked(null);
                    return true;
                }
                return false;
            }
        });

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        btn_signup = (Button) findViewById(R.id.btn_signup_register);
    }

    public boolean signup_check_inputdata() {

        if (et_signup_email.getText().toString().length()*et_signup_password.getText().toString().length()*et_signup_name.getText().toString().length() == 0){
            return false;
        }
        return true;

    }

    public void register_clicked(View v) {

        if (!signup_check_inputdata()){
            Toast.makeText(SignupActivity.this, "항목을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        btn_signup.setText("");
        btn_signup.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        
        mAuth.createUserWithEmailAndPassword(et_signup_email.getText().toString(), et_signup_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        Log.d(PACKAGE_NAME, et_signup_email.getText().toString() +" - " + et_signup_password.getText().toString());
                        if (!task.isSuccessful()){
                            Toast.makeText(SignupActivity.this, "계정 생성 실패!!!!!", Toast.LENGTH_SHORT).show();

                            btn_signup.setText("회원 가입");
                            btn_signup.setEnabled(true);
                            progressBar.setVisibility(View.INVISIBLE);
                        } else {
                            saveUser();
                            Toast.makeText(SignupActivity.this, "계정 생성 완료\n로그인 해주세요", Toast.LENGTH_SHORT).show();

                            finish();
                        }
                    }
                });
    }

    public void saveUser() {
        UserData userData = new UserData(
                mAuth.getCurrentUser().getUid(),
                et_signup_email.getText().toString(),
                et_signup_name.getText().toString()
        );

        Log.d(PACKAGE_NAME, "SignupActivity : 계정 생성 : " + userData.getUid().toString());

        DatabaseReference user = databaseReference.child("users").child(userData.getUid());
        user.setValue(userData);
    }
}
