package com.example.cln;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    TextView welcomeText, navChat, navProfile;
    LinearLayout resourcesCard, aiCard;
    RelativeLayout projectActivityCard, taskActivityCard;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        auth = FirebaseAuth.getInstance();

        welcomeText = findViewById(R.id.welcomeText);
        navChat = findViewById(R.id.navChat);
        navProfile = findViewById(R.id.navProfile);

        resourcesCard = findViewById(R.id.resourcesCard);
        aiCard = findViewById(R.id.aiCard);

        projectActivityCard = findViewById(R.id.projectActivityCard);
        taskActivityCard = findViewById(R.id.taskActivityCard);

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
            return;
        }

        String name = user.getEmail();

        if (name != null && name.contains("@")) {
            name = name.substring(0, name.indexOf("@"));
        }

        welcomeText.setText("Welcome, " + name + " 👋");

        // Chat
        navChat.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ChatActivity.class)));

        // Profile
        navProfile.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // Resources
        resourcesCard.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ResourcesActivity.class)));

        // AI Assistant
        aiCard.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AiActivity.class)));

        // Projects
        projectActivityCard.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProjectsActivity.class)));

        // Tasks
        taskActivityCard.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, TasksActivity.class)));
    }
}