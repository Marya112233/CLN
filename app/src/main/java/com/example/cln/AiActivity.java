package com.example.cln;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.concurrent.TimeUnit;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AiActivity extends AppCompatActivity {

    EditText aiQuestionInput;
    Button aiSendBtn, backHomeBtn;
    ListView aiChatList;

    ArrayList<String> aiMessages;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;
    String currentEmail = "User";

    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    String GEMINI_API_KEY = "AQ.Ab8RN6Jc7JoBq-Zuv2BAYtjBZ3nrxUdOJEhjxniMxPZxHJOHKg";

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
                R.layout.chat_message_item,
                R.id.messageText,
                aiMessages
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.messageText);

                String msg = aiMessages.get(position);

                textView.setTextColor(Color.WHITE);
                textView.setTextSize(15);
                textView.setPadding(16, 14, 16, 14);

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(30);

                if (msg.startsWith("You:")) {
                    bg.setColor(Color.parseColor("#B13DFF"));
                    textView.setGravity(Gravity.END);
                } else {
                    bg.setColor(Color.parseColor("#1A1F3A"));
                    textView.setGravity(Gravity.START);
                }

                textView.setBackground(bg);

                return view;
            }
        };

        aiChatList.setAdapter(adapter);

        loadAiMessages();

        aiSendBtn.setOnClickListener(v -> {
            String question = aiQuestionInput.getText().toString().trim();

            if (question.isEmpty()) {
                aiQuestionInput.setError("Type your question");
                return;
            }

            aiQuestionInput.setText("");

            aiMessages.add("You:\n" + question);
            aiMessages.add("AI:\nThinking...");
            adapter.notifyDataSetChanged();
            aiChatList.setSelection(aiMessages.size() - 1);

            askGemini(question);
        });

        backHomeBtn.setOnClickListener(v -> {
            startActivity(new Intent(AiActivity.this, MainActivity.class));
            finish();
        });
    }

    private void askGemini(String question) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";

            JSONObject textPart = new JSONObject();
            textPart.put("text", question);

            JSONArray parts = new JSONArray();
            parts.put(textPart);

            JSONObject content = new JSONObject();
            content.put("parts", parts);

            JSONArray contents = new JSONArray();
            contents.put(content);

            JSONObject bodyJson = new JSONObject();
            bodyJson.put("contents", contents);

            RequestBody body = RequestBody.create(
                    bodyJson.toString(),
                    MediaType.parse("application/json")
            );

            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("x-goog-api-key", GEMINI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() ->
                            replaceThinking("AI:\nConnection failed: " + e.getMessage())
                    );
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body() != null ? response.body().string() : "";

                    if (!response.isSuccessful()) {
                        runOnUiThread(() ->
                                replaceThinking("AI:\nError: " + responseBody)
                        );
                        return;
                    }

                    try {
                        JSONObject json = new JSONObject(responseBody);
                        String answer = json
                                .getJSONArray("candidates")
                                .getJSONObject(0)
                                .getJSONObject("content")
                                .getJSONArray("parts")
                                .getJSONObject(0)
                                .getString("text");

                        runOnUiThread(() -> {
                            replaceThinking("AI:\n" + answer);
                            saveToFirebase(question, answer);
                        });

                    } catch (Exception e) {
                        runOnUiThread(() ->
                                replaceThinking("AI:\nCould not read AI response.")
                        );
                    }
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void replaceThinking(String aiText) {
        if (aiMessages.size() > 0) {
            aiMessages.remove(aiMessages.size() - 1);
        }

        aiMessages.add(aiText);
        adapter.notifyDataSetChanged();
        aiChatList.setSelection(aiMessages.size() - 1);
    }

    private void saveToFirebase(String question, String answer) {
        Map<String, Object> data = new HashMap<>();
        data.put("userEmail", currentEmail);
        data.put("question", question);
        data.put("answer", answer);
        data.put("timestamp", System.currentTimeMillis());

        db.collection("ai_questions").add(data);
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

                        aiMessages.add("You:\n" + question);
                        aiMessages.add("AI:\n" + answer);
                    });

                    adapter.notifyDataSetChanged();

                    if (aiMessages.size() > 0) {
                        aiChatList.setSelection(aiMessages.size() - 1);
                    }
                });
    }
}