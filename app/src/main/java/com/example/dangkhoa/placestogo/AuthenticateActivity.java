package com.example.dangkhoa.placestogo;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;

/**
 * Created by dangkhoa on 05/02/2018.
 */

public class AuthenticateActivity extends AppCompatActivity {

    private int AUTHENTICATE_MODE;
    private static final int LOGIN_MODE = 1;
    private static final int SIGNUP_MODE = 2;

    private class ViewHolder {
        public EditText emailEditText, passwordEditText;
        public Button submitBtn, forgotPasswordBtn, loginRegisterBtn;
        public ProgressBar progressBar;

        public ViewHolder() {
            emailEditText = findViewById(R.id.emailEditText);
            passwordEditText = findViewById(R.id.passwordEditText);

            submitBtn = findViewById(R.id.submit_btn);
            forgotPasswordBtn = findViewById(R.id.forgot_password_btn);
            loginRegisterBtn = findViewById(R.id.login_register_btn);

            progressBar = findViewById(R.id.authProgressBar);
        }
    }

    private ViewHolder viewHolder;

    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        AUTHENTICATE_MODE = LOGIN_MODE;

        auth = FirebaseAuth.getInstance();

        if (AUTHENTICATE_MODE == LOGIN_MODE && auth.getCurrentUser() != null) {
            startMainActivity();
        }

        viewHolder = new ViewHolder();

        viewHolder.submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = viewHolder.emailEditText.getText().toString().trim();
                final String password = viewHolder.passwordEditText.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.email_missing_message), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (password.isEmpty()) {
                    Toast.makeText(getApplicationContext(), getString(R.string.password_missing_message), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (AUTHENTICATE_MODE == SIGNUP_MODE && password.length() < 8) {
                    Toast.makeText(getApplicationContext(), getString(R.string.password_too_short_message), Toast.LENGTH_SHORT).show();
                    return;
                }

                viewHolder.progressBar.setVisibility(View.VISIBLE);

                if (AUTHENTICATE_MODE == LOGIN_MODE) {
                    auth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    viewHolder.progressBar.setVisibility(View.GONE);

                                    if (!task.isSuccessful()) {

                                        if (password.length() < 8) {
                                            Toast.makeText(getApplicationContext(), getString(R.string.password_too_short_message), Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(getApplicationContext(), getString(R.string.auth_failed), Toast.LENGTH_SHORT).show();
                                        }

                                    } else {
                                        startMainActivity();
                                    }
                                }
                            });
                } else {
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    viewHolder.progressBar.setVisibility(View.GONE);

                                    if (!task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), getString(R.string.signup_failed), Toast.LENGTH_SHORT).show();
                                    } else {
                                        startMainActivity();
                                    }
                                }
                            });
                }
            }
        });

        viewHolder.forgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        viewHolder.loginRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (AUTHENTICATE_MODE == LOGIN_MODE) {
                    viewHolder.submitBtn.setText(getText(R.string.register));
                    viewHolder.loginRegisterBtn.setText(getString(R.string.registered));

                    AUTHENTICATE_MODE = SIGNUP_MODE;

                } else {
                    viewHolder.submitBtn.setText(getText(R.string.login));
                    viewHolder.loginRegisterBtn.setText(getString(R.string.not_registered));

                    AUTHENTICATE_MODE = LOGIN_MODE;
                }

            }
        });
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

}
