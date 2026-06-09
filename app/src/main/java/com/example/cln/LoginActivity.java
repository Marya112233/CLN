package com.example.cln;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    EditText email, password;
    Button loginBtn;
    TextView registerText;
    ProgressBar loading;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginBtn = findViewById(R.id.loginBtn);
        registerText = findViewById(R.id.registerText);
        loading = findViewById(R.id.loading);

        auth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(v -> loginUser());

        registerText.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void loginUser() {

        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();

        if (userEmail.isEmpty()) {
            email.setError("Enter email");
            email.requestFocus();
            return;
        }

        if (userPass.isEmpty()) {
            password.setError("Enter password");
            password.requestFocus();
            return;
        }

        loading.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);

        auth.signInWithEmailAndPassword(userEmail, userPass)
                .addOnSuccessListener(authResult -> {

                    Toast.makeText(LoginActivity.this,
                            "LOGIN SUCCESS",
                            Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(() -> {

                        loading.setVisibility(View.GONE);
                        loginBtn.setEnabled(true);

                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                    }, 1000);
                })

                .addOnFailureListener(e -> {

                    loading.setVisibility(View.GONE);
                    loginBtn.setEnabled(true);

                    Toast.makeText(LoginActivity.this,
                            "Login failed: This account is not registered",
                            Toast.LENGTH_LONG).show();
                });
    }
}