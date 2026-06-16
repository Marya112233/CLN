package com.example.cln;

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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProjectsActivity extends AppCompatActivity {

    EditText projectInput;
    ListView projectsList;
    Button addProjectBtn, uploadProjectBtn, backHomeBtn;

    ArrayList<String> projects;
    ArrayAdapter<String> adapter;

    FirebaseFirestore db;
    StorageReference storageRef;

    ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_projects);

        projectInput = findViewById(R.id.projectInput);
        projectsList = findViewById(R.id.projectsList);
        addProjectBtn = findViewById(R.id.addProjectBtn);
        uploadProjectBtn = findViewById(R.id.uploadProjectBtn);
        backHomeBtn = findViewById(R.id.backHomeBtn);

        db = FirebaseFirestore.getInstance();
        storageRef = FirebaseStorage.getInstance().getReference("project_files");

        projects = new ArrayList<>();

        adapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                projects
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

        projectsList.setAdapter(adapter);

        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK &&
                            result.getData() != null &&
                            result.getData().getData() != null) {

                        Uri fileUri = result.getData().getData();
                        uploadFileToFirebase(fileUri);
                    }
                }
        );

        loadProjects();

        addProjectBtn.setOnClickListener(v -> addProjectName());

        uploadProjectBtn.setOnClickListener(v -> openFilePicker());

        backHomeBtn.setOnClickListener(v -> {
            startActivity(new Intent(ProjectsActivity.this, MainActivity.class));
            finish();
        });
    }

    private void addProjectName() {
        String projectName = projectInput.getText().toString().trim();

        if (projectName.isEmpty()) {
            projectInput.setError("Enter project name");
            return;
        }

        Map<String, Object> project = new HashMap<>();
        project.put("projectName", projectName);
        project.put("type", "Text Project");
        project.put("fileUrl", "");
        project.put("timestamp", System.currentTimeMillis());

        db.collection("projects")
                .add(project)
                .addOnSuccessListener(documentReference -> {
                    projectInput.setText("");
                    Toast.makeText(this, "Project saved", Toast.LENGTH_SHORT).show();
                    loadProjects();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Not saved: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select Project File"));
    }

    private void uploadFileToFirebase(Uri fileUri) {
        String fileName = "project_" + System.currentTimeMillis();

        StorageReference fileRef = storageRef.child(fileName);

        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();

        fileRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot ->
                        fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {

                            Map<String, Object> project = new HashMap<>();
                            project.put("projectName", "Uploaded Project File");
                            project.put("type", "Uploaded File");
                            project.put("fileUrl", downloadUri.toString());
                            project.put("timestamp", System.currentTimeMillis());

                            db.collection("projects")
                                    .add(project)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "File uploaded", Toast.LENGTH_SHORT).show();
                                        loadProjects();
                                    });
                        })
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void loadProjects() {
        db.collection("projects")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    projects.clear();

                    queryDocumentSnapshots.forEach(document -> {
                        String projectName = document.getString("projectName");
                        String type = document.getString("type");

                        if (projectName == null) projectName = "Project";
                        if (type == null) type = "Project";

                        projects.add("📁 " + projectName + "\n" + type);
                    });

                    adapter.notifyDataSetChanged();
                });
    }
}