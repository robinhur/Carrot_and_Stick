package com.huza.carrot_and_stick;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        et_signup_email = (EditText) findViewById(R.id.et_signup_email);
        et_signup_password = (EditText) findViewById(R.id.et_signup_password);
        et_signup_name = (EditText) findViewById(R.id.et_signup_name);
        
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
        
    }

    public void register_clicked(View v) {
        mAuth.createUserWithEmailAndPassword(et_signup_email.getText().toString(), et_signup_password.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(PACKAGE_NAME, et_signup_email.getText().toString() +" - " + et_signup_password.getText().toString());
                        if (!task.isSuccessful()){
                            Toast.makeText(SignupActivity.this, "계정 생성 실패!!!!!", Toast.LENGTH_SHORT).show();
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
