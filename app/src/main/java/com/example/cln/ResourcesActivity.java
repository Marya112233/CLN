package com.example.cln;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
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

public class ResourcesActivity extends AppCompatActivity {

    EditText resourceNameInput, resourceLinkInput;
    ListView resourcesList;
    Button addResourceBtn, backHomeBtn;

    ArrayList<String> resources;
    ArrayList<String> resourceLinks;

    ArrayAdapter<String> adapter;

    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resources);

        resourceNameInput = findViewById(R.id.resourceNameInput);
        resourceLinkInput = findViewById(R.id.resourceLinkInput);
        resourcesList = findViewById(R.id.resourcesList);
        addResourceBtn = findViewById(R.id.addResourceBtn);
        backHomeBtn = findViewById(R.id.backHomeBtn);

        db = FirebaseFirestore.getInstance();

        resources = new ArrayList<>();
        resourceLinks = new ArrayList<>();

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                resources
        ) {
            @Override
            public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
                android.view.View view = super.getView(position, convertView, parent);
                TextView textView = view.findViewById(android.R.id.text1);

                textView.setTextColor(Color.WHITE);
                textView.setTextSize(16);
                textView.setPadding(22, 22, 22, 22);
                textView.setBackgroundColor(Color.parseColor("#1A1F3A"));

                return view;
            }
        };

        resourcesList.setAdapter(adapter);

        addResourceBtn.setOnClickListener(v -> addResource());

        resourcesList.setOnItemClickListener((parent, view, position, id) -> {
            String link = resourceLinks.get(position);
            openResourceLink(link);
        });

        backHomeBtn.setOnClickListener(v -> {
            startActivity(new Intent(ResourcesActivity.this, MainActivity.class));
            finish();
        });

        loadResources();
    }

    private void openResourceLink(String link) {
        if (link == null || link.trim().isEmpty()) {
            Toast.makeText(this, "No link found", Toast.LENGTH_SHORT).show();
            return;
        }

        link = link.trim();

        if (!link.startsWith("http://") && !link.startsWith("https://")) {
            link = "https://" + link;
        }

        try {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);

            Intent chooser = Intent.createChooser(browserIntent, "Open Resource");
            startActivity(chooser);

        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, "No browser found to open this link", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open link", Toast.LENGTH_LONG).show();
        }
    }

    private void addResource() {
        String name = resourceNameInput.getText().toString().trim();
        String link = resourceLinkInput.getText().toString().trim();

        if (name.isEmpty()) {
            resourceNameInput.setError("Enter resource name");
            return;
        }

        if (link.isEmpty()) {
            resourceLinkInput.setError("Enter resource link");
            return;
        }

        Map<String, Object> resource = new HashMap<>();
        resource.put("resourceName", name);
        resource.put("resourceLink", link);
        resource.put("timestamp", System.currentTimeMillis());

        db.collection("resources")
                .add(resource)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Resource saved", Toast.LENGTH_SHORT).show();

                    resourceNameInput.setText("");
                    resourceLinkInput.setText("");

                    loadResources();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Not saved: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void loadResources() {
        db.collection("resources")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    resources.clear();
                    resourceLinks.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        String resourceName = document.getString("resourceName");
                        String resourceLink = document.getString("resourceLink");

                        if (resourceName == null) resourceName = "Resource";
                        if (resourceLink == null) resourceLink = "";

                        resources.add("📚 " + resourceName + "\n🔗 " + resourceLink);
                        resourceLinks.add(resourceLink);
                    });

                    adapter.notifyDataSetChanged();
                });
    }
}