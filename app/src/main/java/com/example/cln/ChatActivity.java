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

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    ListView chatList;
    EditText messageInput;
    Button sendBtn;
    TextView backHomeBtn;

    ArrayList<String> messages;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String currentEmail = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        chatList = findViewById(R.id.chatList);
        messageInput = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);
        backHomeBtn = findViewById(R.id.backHomeBtn);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            currentEmail = user.getEmail();
        }

        messages = new ArrayList<>();

        adapter = new ArrayAdapter<String>(
                this,
                R.layout.chat_bubble_item,
                R.id.messageText,
                messages
        ) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(R.id.messageText);

                String msg = messages.get(position);

                textView.setTextColor(Color.WHITE);
                textView.setTextSize(15);
                textView.setPadding(18, 14, 18, 14);

                GradientDrawable bg = new GradientDrawable();
                bg.setCornerRadius(28);

                if (!currentEmail.isEmpty() && msg.startsWith(currentEmail)) {
                    bg.setColor(Color.parseColor("#7B3FF2"));
                    textView.setGravity(Gravity.END);
                } else {
                    bg.setColor(Color.parseColor("#1A1F3A"));
                    textView.setGravity(Gravity.START);
                }

                textView.setBackground(bg);
                return view;
            }
        };

        chatList.setAdapter(adapter);

        loadMessages();

        sendBtn.setOnClickListener(v -> sendMessage());

        backHomeBtn.setOnClickListener(v -> {
            Intent intent = new Intent(ChatActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void sendMessage() {
        String text = messageInput.getText().toString().trim();

        if (text.isEmpty()) {
            messageInput.setError("Type message");
            return;
        }

        String senderName = currentEmail.isEmpty() ? "User" : currentEmail;

        Map<String, Object> message = new HashMap<>();
        message.put("senderName", senderName);
        message.put("message", text);
        message.put("timestamp", System.currentTimeMillis());

        db.collection("messages")
                .add(message)
                .addOnSuccessListener(documentReference -> {
                    messageInput.setText("");
                    loadMessages();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Message not saved: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void loadMessages() {
        db.collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    messages.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        String senderName = document.getString("senderName");
                        String messageText = document.getString("message");

                        if (senderName == null) senderName = "User";
                        if (messageText == null) messageText = "";

                        messages.add(senderName + ":\n" + messageText);
                    });

                    adapter.notifyDataSetChanged();

                    if (messages.size() > 0) {
                        chatList.setSelection(messages.size() - 1);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Load failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}