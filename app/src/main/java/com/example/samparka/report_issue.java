package com.example.samparka;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.firestore.FirebaseFirestore;



import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class report_issue extends AppCompatActivity {

    Spinner spinnerIssueType;
    EditText etDescription;
    Button btnSubmit;
    LinearLayout uploadSection;
    ImageView imagePreview;

    Uri imageUri = null;
    File cameraImageFile;

    FirebaseFirestore db;

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;

    // ---------------- CLOUDINARY SETUP ----------------
    Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dvxnedfzr",
            "api_key", "dummy",     // Not required for unsigned preset
            "api_secret", "dummy"   // Not required for unsigned preset
    ));

    String uploadPreset = "samparka_unsigned";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        uploadSection = findViewById(R.id.uploadSection);
        imagePreview = findViewById(R.id.imagePreview);

        db = FirebaseFirestore.getInstance();

        // Dropdown Data
        String[] issues = {"Roads & Infrastructure", "Water Supply", "Electricity",
                "Waste Management", "Drainage", "Health & Sanitation", "Other"};
        spinnerIssueType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, issues));

        uploadSection.setOnClickListener(v -> showImageSourceDialog());
        btnSubmit.setOnClickListener(v -> uploadIssue());
    }

    // ---------------- SELECT IMAGE SOURCE ----------------
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};
        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermission();
                    else checkStoragePermission();
                })
                .show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else openCamera();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_CODE);
            } else openGallery();
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else openGallery();
        }
    }

    // ---------------- OPEN CAMERA ----------------
    private void openCamera() {
        try {
            cameraImageFile = File.createTempFile("issue_", ".jpg",
                    getExternalFilesDir("Pictures"));

            imageUri = FileProvider.getUriForFile(
                    this, getPackageName() + ".fileprovider", cameraImageFile);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Camera error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Camera Result
    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Glide.with(this).load(imageUri).into(imagePreview);
                        }
                    });

    // ---------------- OPEN GALLERY ----------------
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // Gallery Result
    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK &&
                                result.getData() != null) {

                            imageUri = result.getData().getData();
                            Glide.with(this).load(imageUri).into(imagePreview);
                        }
                    });

    // ---------------- UPLOAD ISSUE ----------------
    private void uploadIssue() {

        String issueType = spinnerIssueType.getSelectedItem().toString();
        String description = etDescription.getText().toString().trim();

        if (description.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.show();

        if (imageUri == null) {
            saveIssueToFirestore(issueType, description, null);
            dialog.dismiss();
            return;
        }

        // Upload Image in Background
        new Thread(() -> {
            try {
                String imagePath = getImageFilePath();

                Map uploadResult = cloudinary.uploader().upload(
                        new File(imagePath),
                        ObjectUtils.asMap(
                                "upload_preset", uploadPreset,
                                "folder", "samparka"
                        )
                );

                String imageUrl = uploadResult.get("secure_url").toString();

                runOnUiThread(() -> {
                    dialog.dismiss();
                    saveIssueToFirestore(issueType, description, imageUrl);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String getImageFilePath() {
        if (cameraImageFile != null)
            return cameraImageFile.getAbsolutePath();

        return RealPathUtil.getRealPath(this, imageUri);
    }

    // ---------------- SAVE FIRESTORE ----------------
    private void saveIssueToFirestore(String issueType, String description, String imageUrl) {

        Map<String, Object> issue = new HashMap<>();
        issue.put("type", issueType);
        issue.put("description", description);
        issue.put("imageUrl", imageUrl);
        issue.put("timestamp", System.currentTimeMillis());

        db.collection("issues")
                .add(issue)
                .addOnSuccessListener(doc ->
                        Toast.makeText(this, "Issue submitted!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    // ---------------- PERMISSIONS ----------------
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        // ✅ Always call the super method first (fixes the warning)
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CAMERA_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Camera permission granted
            openCamera();

        } else if (requestCode == STORAGE_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // Storage permission granted
            openGallery();

        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

}
