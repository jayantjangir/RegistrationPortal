package com.intellimind.registrationportal;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class CreateAccountActivity extends AppCompatActivity {

    private ProgressBar regprogressbar;
    private EditText regemail, regpassword, regpassword2;
    private Button regbutton, regloginbutton;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide(); //hide the title bar
        setContentView(R.layout.activity_create_account);

        regprogressbar = findViewById(R.id.regprogressbar);
        regemail = findViewById(R.id.regemail);
        regpassword = findViewById(R.id.regpassword);
        regbutton = findViewById(R.id.regbutton);
        regloginbutton = findViewById(R.id.regloginbutton);
        regpassword2 = findViewById(R.id.regpassword2);
        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("fr");
        regloginbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent sendtologin = new Intent(CreateAccountActivity.this, LoginActivity.class);
                startActivity(sendtologin);
                finish();
            }
        });

        regbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = regemail.getText().toString();
                String password = regpassword.getText().toString();
                String cpassword = regpassword2.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(cpassword)) {
                    if (password.equals(cpassword) && password.length() >= 8) {
                        regprogressbar.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                if (task.isSuccessful()) {
                                    Toast.makeText(CreateAccountActivity.this, "Account Created", Toast.LENGTH_SHORT).show();
                                    mAuth.getCurrentUser().sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(CreateAccountActivity.this, "Verificaion email sent to : " + FirebaseAuth.getInstance().getCurrentUser().getEmail(), Toast.LENGTH_LONG).show();
                                                        Intent sendtomain = new Intent(CreateAccountActivity.this, LoginActivity.class);
                                                        startActivity(sendtomain);
                                                        finish();
                                                    } else{
                                                        Toast.makeText(CreateAccountActivity.this,"Failed to sent verification email",Toast.LENGTH_SHORT).show();
                                                        Intent sendtomain = new Intent(CreateAccountActivity.this, LoginActivity.class);
                                                        startActivity(sendtomain);
                                                        finish();
                                                    }
                                                }
                                            });
                                } else {
                                    String error = task.getException().getMessage();
                                    Toast.makeText(CreateAccountActivity.this, "Error:" + error, Toast.LENGTH_SHORT).show();
                                }
                                regprogressbar.setVisibility(View.INVISIBLE);

                            }
                        });
                    } else {
                        Toast.makeText(CreateAccountActivity.this, "Passwords do not match or too short", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Please fill the form completely!", Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currenruser = mAuth.getCurrentUser();
        if (currenruser != null && currenruser.isEmailVerified() ) {
            // user is logged in and no need to register/login
            Intent sendtomain = new Intent(CreateAccountActivity.this, MainActivity.class);
            startActivity(sendtomain);
            finish();
        }
    }

    public void close(){
        finish();
    }
}
