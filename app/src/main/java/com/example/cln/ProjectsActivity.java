package com.example.cln;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ProjectsActivity extends AppCompatActivity {

    ListView projectsList;
    Button addProjectBtn, backHomeBtn;

    ArrayList<String> projects;
    ArrayAdapter<String> adapter;
    int projectCount = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        projectsList = findViewById(R.id.projectsList);
        addProjectBtn = findViewById(R.id.addProjectBtn);
        backHomeBtn = findViewById(R.id.backHomeBtn);

        projects = new ArrayList<>();

        projects.add("📁 CLN Mobile App Development");
        projects.add("📁 UI Design Improvement");
        projects.add("📁 Firebase Chat Integration");

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                projects
        ) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                android.widget.TextView textView = view.findViewById(android.R.id.text1);
                textView.setTextColor(Color.WHITE);
                textView.setTextSize(17);
                textView.setPadding(22, 22, 22, 22);
                textView.setBackgroundColor(Color.parseColor("#1A1F3A"));
                return view;
            }
        };

        projectsList.setAdapter(adapter);

        addProjectBtn.setOnClickListener(v -> {
            projectCount++;
            projects.add("📁 New Project " + projectCount);
            adapter.notifyDataSetChanged();
        });

        backHomeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ProjectsActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }
}