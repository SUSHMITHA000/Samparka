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
import androidx.annotation.RequiresPermission;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
    TextView tvLocation, userNameSmall;
    Button btnSubmit;
    LinearLayout uploadSection;
    ImageView imagePreview, userProfileSmall, profileIcon;

    Uri imageUri = null;
    File cameraImageFile;
    String currentAddress = "Unable to detect location. Move outside.";

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

    FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        uploadSection = findViewById(R.id.uploadSection);
        imagePreview = findViewById(R.id.imagePreview);
        tvLocation = findViewById(R.id.tvLocation);

        // Header elements
        userProfileSmall = findViewById(R.id.userProfileSmall);
        userNameSmall = findViewById(R.id.userNameSmall);
        profileIcon = findViewById(R.id.profileIcon);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        String[] issues = {"Roads & Infrastructure", "Water Supply", "Electricity",
                "Waste Management", "Drainage", "Health & Sanitation", "Other"};
        spinnerIssueType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, issues));

        uploadSection.setOnClickListener(v -> showImageSourceDialog());
        btnSubmit.setOnClickListener(v -> uploadIssue());

        requestLocationPermission();
        loadUserProfileHeader();

        if (userProfileSmall != null)
            userProfileSmall.setOnClickListener(v -> startActivity(new Intent(report_issue.this, ProfileActivity.class)));

        if (profileIcon != null)
            profileIcon.setOnClickListener(v -> startActivity(new Intent(report_issue.this, ProfileActivity.class)));
    }

    // ---------------- USER HEADER (name + photo) ----------------
    private void loadUserProfileHeader() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener((DocumentSnapshot doc) -> {
                    if (doc.exists()) {

                        // Fetch name
                        String name = doc.getString("name");
                        if (name != null && !name.isEmpty()) {
                            userNameSmall.setText("Hi " + name);
                        } else if (user.getDisplayName() != null) {
                            userNameSmall.setText("Hi " + user.getDisplayName());
                        }

                        // Fetch photo
                        String photoUrl = doc.getString("photoUrl");
                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this)
                                    .load(photoUrl)
                                    .circleCrop()
                                    .into(userProfileSmall);
                        }
                    } else {
                        // fallback to FirebaseUser
                        if (user.getDisplayName() != null)
                            userNameSmall.setText("Hi " + user.getDisplayName());
                    }
                })
                .addOnFailureListener(e -> {
                    if (user.getDisplayName() != null)
                        userNameSmall.setText("Hi " + user.getDisplayName());
                });
    }

    // ---------------- LOCATION PERMISSION ----------------
    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
        } else {
            fetchCurrentLocation();
        }
    }

    // ---------------- FETCH CURRENT LOCATION ----------------
    @SuppressLint("SetTextI18n")
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
    private void fetchCurrentLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getAddressFromCoordinates(location.getLatitude(), location.getLongitude());
                    } else {
                        tvLocation.setText("Unable to detect location.");
                    }
                })
                .addOnFailureListener(e -> tvLocation.setText("Unable to detect location."));
    }

    // ---------------- COORDINATES → ADDRESS ----------------
    @SuppressLint("SetTextI18n")
    private void getAddressFromCoordinates(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lon, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                currentAddress = address.getAddressLine(0);
                tvLocation.setText("Auto-detected: " + currentAddress);
            }
        } catch (Exception e) {
            tvLocation.setText("Unable to detect location.");
        }
    }

    // ---------------- IMAGE PICKER ----------------
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

    // ---------------- CAMERA ----------------
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

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            Glide.with(this).load(imageUri).into(imagePreview);
                        }
                    });

    // ---------------- GALLERY ----------------
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK &&
                                result.getData() != null) {

                            imageUri = result.getData().getData();
                            Glide.with(this).load(imageUri).into(imagePreview);
                        }
                    });

    // ---------------- SUBMIT ISSUE ----------------
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

        new Thread(() -> {
            try {
                String imagePath = getImageFilePath();

                Map uploadResult = cloudinary.uploader().upload(
                        new File(imagePath),
                        ObjectUtils.asMap("folder", "samparka")
                );

                String imageUrl = uploadResult.get("secure_url").toString();

                runOnUiThread(() -> {
                    dialog.dismiss();
                    saveIssueToFirestore(issueType, description, imageUrl);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this,
                            "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String getImageFilePath() {
        if (cameraImageFile != null)
            return cameraImageFile.getAbsolutePath();

        return RealPathUtil.getRealPath(this, imageUri);
    }

    // ---------------- SAVE TO FIRESTORE ----------------
    private void saveIssueToFirestore(String issueType, String description, String imageUrl) {

        Map<String, Object> issue = new HashMap<>();
        issue.put("type", issueType);
        issue.put("description", description);
        issue.put("imageUrl", imageUrl);
        issue.put("address", currentAddress);
        issue.put("timestamp", System.currentTimeMillis());

        FirebaseUser user = auth.getCurrentUser();
        if (user != null) issue.put("userId", user.getUid());

        db.collection("issues")
                .add(issue)
                .addOnSuccessListener(doc ->
                        Toast.makeText(this, "Issue submitted!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ---------------- PERMISSION RESULTS ----------------
    @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
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

        } else if (requestCode == STORAGE_PERMISSION_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        }
    }
}
