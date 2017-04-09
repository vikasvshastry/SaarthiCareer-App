package com.saarthicareer.saarthicareer.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.saarthicareer.saarthicareer.R;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firebaseAuth = FirebaseAuth.getInstance();

        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Button buttonLogin = (Button) findViewById(R.id.btn_login);
        final EditText editTextEmail = (EditText) findViewById(R.id.input_email);
        final EditText editTextPassword = (EditText) findViewById(R.id.input_password);
        final Button buttonSignup = (Button) findViewById(R.id.btn_signup);
        final TextView signuptext = (TextView) findViewById(R.id.link_to_register);
        final TextView forgotPassword = (TextView) findViewById(R.id.forgot_password);

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_password_reset);
        final TextView select = (TextView) dialog.findViewById(R.id.select);
        final EditText editText = (EditText) dialog.findViewById(R.id.mail);

        //for forgot password
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                select.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String string = editText.getText().toString().trim();
                        dialog.dismiss();
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.sendPasswordResetEmail(string).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "Email sent", Toast.LENGTH_SHORT).show();
                                }
                                else
                                {
                                    Toast.makeText(LoginActivity.this, "Currently unable to process request", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();
                userLogin(email,password);
            }
        });
        buttonSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            }
        });
        signuptext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
            }
        });
    }

    private void userLogin(String email,String password){
        final ProgressDialog progressDialog = new ProgressDialog(this);

        if(TextUtils.isEmpty(email)){
            Toast.makeText(LoginActivity.this, "Please enter Email", Toast.LENGTH_SHORT).show();
            return;
        }
        if(TextUtils.isEmpty(password)){
            Toast.makeText(LoginActivity.this, "Please enter Password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging In...");
        progressDialog.show();

        try {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressDialog.dismiss();
                            if (task.isSuccessful()) {
                                //start main activity
                                finish();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(LoginActivity.this, "Failed to login", Toast.LENGTH_SHORT).show();

                            }
                        }
                    });
        }catch (Exception e){
            Toast.makeText(this, "Unknown error. Contact Dev", Toast.LENGTH_SHORT).show();
        }
    }
}
