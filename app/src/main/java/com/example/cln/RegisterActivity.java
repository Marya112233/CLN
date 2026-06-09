package com.example.cln;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class RegisterActivity extends AppCompatActivity {

    EditText name, email, password, confirmPassword;
    Button registerBtn;
    TextView loginText;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirmPassword = findViewById(R.id.confirmPassword);
        registerBtn = findViewById(R.id.registerBtn);
        loginText = findViewById(R.id.loginText);

        auth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(v -> registerUser());

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String userName = name.getText().toString().trim();
        String userEmail = email.getText().toString().trim();
        String userPass = password.getText().toString().trim();
        String confirmPass = confirmPassword.getText().toString().trim();

        if (userName.isEmpty()) {
            name.setError("Enter name");
            return;
        }

        if (userEmail.isEmpty()) {
            email.setError("Enter email");
            return;
        }

        if (userPass.isEmpty()) {
            password.setError("Enter password");
            return;
        }

        if (userPass.length() < 6) {
            password.setError("Password must be at least 6 characters");
            return;
        }

        if (!userPass.equals(confirmPass)) {
            confirmPassword.setError("Password not match");
            return;
        }

        registerBtn.setEnabled(false);

        auth.createUserWithEmailAndPassword(userEmail, userPass)
                .addOnSuccessListener(authResult -> {

                    FirebaseUser user = auth.getCurrentUser();

                    if (user != null) {
                        UserProfileChangeRequest profileUpdates =
                                new UserProfileChangeRequest.Builder()
                                        .setDisplayName(userName)
                                        .build();

                        user.updateProfile(profileUpdates)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    registerBtn.setEnabled(true);
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    registerBtn.setEnabled(true);
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}