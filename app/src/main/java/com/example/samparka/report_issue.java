package com.example.samparka;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class report_issue extends AppCompatActivity {

    Spinner spinnerIssueType;
    EditText etDescription;
    TextView tvLocation;
    Button btnSubmit, btnUploadPhoto;
    ImageView imagePreview;

    Uri imageUri = null;
    File cameraImageFile;

    String currentAddress = "Unable to detect location.";

    FirebaseFirestore db;
    FirebaseAuth auth;

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private static final int LOCATION_PERMISSION_CODE = 200;

    // Cloudinary setup
    Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dvxnedfzr",
            "api_key", "939937542917265",
            "api_secret", "swOxStKjCgXcjh6AaGcvprTVoH0"
    ));

    // Location
    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        // Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // UI
        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etDescription = findViewById(R.id.etDescription);
        tvLocation = findViewById(R.id.tvLocation);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        imagePreview = findViewById(R.id.imagePreview);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Spinner
        String[] issues = {"Pot Hole", "Street Light", "Garbage"};
        spinnerIssueType.setAdapter(new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                issues
        ));

        btnUploadPhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSubmit.setOnClickListener(v -> uploadIssue());

        requestLocationPermission();
    }

    // ---------------- LOCATION ----------------
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE);
        } else {
            fetchCurrentLocation();
        }
    }

    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getAddressFromCoordinates(
                                location.getLatitude(),
                                location.getLongitude());
                    } else {
                        tvLocation.setText("Unable to detect location");
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void getAddressFromCoordinates(double lat, double lon) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);

            if (list != null && !list.isEmpty()) {
                currentAddress = list.get(0).getAddressLine(0);
                tvLocation.setText("Auto-detected: " + currentAddress);
            }
        } catch (Exception e) {
            tvLocation.setText("Unable to detect location");
        }
    }

    // ---------------- IMAGE PICK ----------------
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermission();
                    else checkStoragePermission();
                }).show();
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else openCamera();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION_CODE);
            } else openGallery();
        } else {
            openGallery();
        }
    }

    private void openCamera() {
        try {
            cameraImageFile = File.createTempFile(
                    "issue_", ".jpg", getExternalFilesDir("Pictures"));

            imageUri = FileProvider.getUriForFile(
                    this,
                    getPackageName() + ".fileprovider",
                    cameraImageFile
            );

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraLauncher.launch(intent);

        } catch (IOException e) {
            Toast.makeText(this, "Camera error", Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            showPreviewImage(imageUri);
                        }
                    });

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                            imageUri = result.getData().getData();
                            showPreviewImage(imageUri);
                        }
                    });

    private void showPreviewImage(Uri uri) {
        if (uri == null) return;

        imagePreview.setVisibility(ImageView.VISIBLE);

        Glide.with(this)
                .load(uri)
                .centerCrop()
                .into(imagePreview);
    }

    // ---------------- UPLOAD ISSUE ----------------
    private void uploadIssue() {
        String type = spinnerIssueType.getSelectedItem().toString();
        String desc = etDescription.getText().toString().trim();

        if (desc.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri == null) {
            Toast.makeText(this, "Upload image", Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.show();

        new Thread(() -> {
            try {
                String path = cameraImageFile != null
                        ? cameraImageFile.getAbsolutePath()
                        : RealPathUtil.getRealPath(this, imageUri);

                Map result = cloudinary.uploader().upload(
                        new File(path),
                        ObjectUtils.asMap("folder", "samparka")
                );

                String imageUrl = result.get("secure_url").toString();

                runOnUiThread(() -> {
                    dialog.dismiss();
                    saveIssueToFirestore(type, desc, imageUrl);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this,
                            "Upload failed",
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    // ---------------- SAVE ISSUE ----------------
    private void saveIssueToFirestore(String type, String desc, String imageUrl) {

        String uid = auth.getUid();

        Map<String, Object> issue = new HashMap<>();
        issue.put("userId", uid);
        issue.put("type", type);
        issue.put("description", desc);
        issue.put("imageUrl", imageUrl);
        issue.put("address", currentAddress);
        issue.put("status", "Pending");
        issue.put("timestamp", System.currentTimeMillis());

        db.collection("issues")
                .add(issue)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this,
                            "Issue Submitted",
                            Toast.LENGTH_SHORT).show();

                    startActivity(
                            new Intent(report_issue.this,
                                    ComplaintsActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show());
    }

    // ---------------- PERMISSIONS ----------------
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchCurrentLocation();
        }

        if (requestCode == CAMERA_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        }

        if (requestCode == STORAGE_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }
}
