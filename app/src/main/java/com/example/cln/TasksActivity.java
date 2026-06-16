package com.example.cln;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TasksActivity extends AppCompatActivity {

    EditText taskInput;
    Button addTaskBtn, homeBtn;
    ListView tasksList;

    ArrayList<String> tasks;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

        taskInput = findViewById(R.id.taskInput);
        addTaskBtn = findViewById(R.id.addTaskBtn);
        homeBtn = findViewById(R.id.homeBtn);
        tasksList = findViewById(R.id.tasksList);

        db = FirebaseFirestore.getInstance();

        tasks = new ArrayList<>();

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                tasks
        ) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);

                textView.setTextColor(Color.WHITE);
                textView.setTextSize(16);
                textView.setPadding(20, 18, 20, 18);
                textView.setBackgroundColor(Color.parseColor("#1A1F3A"));

                return view;
            }
        };

        tasksList.setAdapter(adapter);

        loadTasks();

        addTaskBtn.setOnClickListener(v -> addTask());

        tasksList.setOnItemClickListener((parent, view, position, id) -> {
            Toast.makeText(this, "Task selected: " + tasks.get(position), Toast.LENGTH_SHORT).show();
        });

        homeBtn.setOnClickListener(v -> {
            startActivity(new Intent(TasksActivity.this, MainActivity.class));
            finish();
        });
    }

    private void addTask() {
        String taskText = taskInput.getText().toString().trim();

        if (taskText.isEmpty()) {
            taskInput.setError("Enter task");
            return;
        }

        Map<String, Object> task = new HashMap<>();
        task.put("taskName", taskText);
        task.put("timestamp", System.currentTimeMillis());

        db.collection("tasks")
                .add(task)
                .addOnSuccessListener(documentReference -> {
                    taskInput.setText("");
                    Toast.makeText(this, "Task saved", Toast.LENGTH_SHORT).show();
                    loadTasks();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Not saved: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void loadTasks() {
        db.collection("tasks")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tasks.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        String taskName = document.getString("taskName");
                        if (taskName != null) {
                            tasks.add("✅ " + taskName);
                        }
                    });

                    adapter.notifyDataSetChanged();
                });
    }
}