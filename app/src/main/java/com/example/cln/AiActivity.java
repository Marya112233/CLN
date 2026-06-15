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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AiActivity extends AppCompatActivity {

    EditText aiQuestionInput;
    Button aiSendBtn, backHomeBtn;
    ListView aiChatList;

    ArrayList<String> aiMessages;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String currentEmail = "User";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai);

        aiQuestionInput = findViewById(R.id.aiQuestionInput);
        aiSendBtn = findViewById(R.id.aiSendBtn);
        backHomeBtn = findViewById(R.id.backHomeBtn);
        aiChatList = findViewById(R.id.aiChatList);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            currentEmail = user.getEmail();
        }

        aiMessages = new ArrayList<>();

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                aiMessages
        ) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);

                textView.setTextColor(Color.WHITE);
                textView.setTextSize(15);
                textView.setPadding(18, 18, 18, 18);
                textView.setBackgroundColor(Color.parseColor("#1A1F3A"));

                return view;
            }
        };

        aiChatList.setAdapter(adapter);

        loadAiMessages();

        aiSendBtn.setOnClickListener(v -> sendAiQuestion());

        backHomeBtn.setOnClickListener(v -> {
            startActivity(new Intent(AiActivity.this, MainActivity.class));
            finish();
        });
    }

    private void sendAiQuestion() {
        String question = aiQuestionInput.getText().toString().trim();

        if (question.isEmpty()) {
            aiQuestionInput.setError("Type your question");
            return;
        }

        String answer = generateAnswer(question.toLowerCase());

        Map<String, Object> data = new HashMap<>();
        data.put("userEmail", currentEmail);
        data.put("question", question);
        data.put("answer", answer);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("ai_questions")
                .add(data)
                .addOnSuccessListener(documentReference -> {
                    aiQuestionInput.setText("");
                    loadAiMessages();
                    Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Not saved: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void loadAiMessages() {
        db.collection("ai_questions")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    aiMessages.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        String question = document.getString("question");
                        String answer = document.getString("answer");

                        if (question == null) question = "";
                        if (answer == null) answer = "";

                        aiMessages.add("You:\n" + question + "\n\nAI:\n" + answer);
                    });

                    adapter.notifyDataSetChanged();

                    if (aiMessages.size() > 0) {
                        aiChatList.setSelection(aiMessages.size() - 1);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private String generateAnswer(String question) {

        if (question.contains("java")) {
            return "Java is used in Android development to create application logic, buttons, navigation, and user interaction.";
        }

        if (question.contains("firebase")) {
            return "Firebase is a cloud platform used to manage authentication, store data, and connect the application with online services.";
        }

        if (question.contains("android")) {
            return "Android is a mobile operating system. In this project, Android Studio is used to design and build the CLN mobile application.";
        }

        if (question.contains("chat")) {
            return "The chat feature allows users to communicate, ask questions, and share learning ideas inside the CLN application.";
        }

        if (question.contains("project")) {
            return "Projects help users apply what they learn in real practice. They also support collaboration and skill development.";
        }

        if (question.contains("task")) {
            return "Tasks help users organize their work into smaller steps, making it easier to complete activities on time.";
        }

        if (question.contains("profile")) {
            return "The profile page displays the user's account information and allows the user to log out safely.";
        }

        if (question.contains("login")) {
            return "The login page allows registered users to access the CLN application using their email and password.";
        }

        if (question.contains("register") || question.contains("sign up")) {
            return "The register page allows new users to create an account before using the application.";
        }

        if (question.contains("cln")) {
            return "CLN is a Collaborative Learning and Networking application designed to help students learn, communicate, and manage projects.";
        }

        if (question.contains("hello") || question.contains("hi")) {
            return "Hello! I am your CLN AI Learning Assistant. How can I help you today?";
        }

        if (question.contains("how are you")) {
            return "I am doing well. I am ready to help you with learning, projects, tasks, and programming questions.";
        }

        return "This AI Assistant gives simple learning support. It can help with programming, Firebase, Android, projects, tasks, and general study questions.";
    }
}