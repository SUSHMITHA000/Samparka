package com.example.samparka;

import com.google.firebase.auth.FirebaseAuth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
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
    Button btnSubmit, btnUploadPhoto;
    ImageView imagePreview, userProfileSmall;
    LinearLayout previewContainer;

    Uri imageUri = null;
    File cameraImageFile;

    String currentAddress = "Unable to detect location.";

    FirebaseFirestore db;

    private static final int CAMERA_PERMISSION_CODE = 101;
    private static final int STORAGE_PERMISSION_CODE = 102;
    private static final int LOCATION_PERMISSION_CODE = 200;

    private ImageClassifier imageClassifier;
    private boolean isImageValid = false; // will be set by classifier

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

        // UI INIT
        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etDescription = findViewById(R.id.etDescription);
        tvLocation = findViewById(R.id.tvLocation);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        previewContainer = findViewById(R.id.previewContainer);
        imagePreview = findViewById(R.id.imagePreview);
        userNameSmall = findViewById(R.id.userNameSmall);
        userProfileSmall = findViewById(R.id.userProfileSmall);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Init classifier
        try {
            imageClassifier = new ImageClassifier(this);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Model load failed", Toast.LENGTH_SHORT).show();
        }

        // Dropdown data
        String[] issues = {
                "Pot Hole", "Street Light", "Drainage"
        };
        spinnerIssueType.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, issues));

        btnUploadPhoto.setOnClickListener(v -> showImageSourceDialog());
        btnSubmit.setOnClickListener(v -> uploadIssue());

        loadUserTopBar();
        requestLocationPermission();
    }

    // Load user name & photo at top bar
    private void loadUserTopBar() {
        String emailKey = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        db.collection("users").document(emailKey)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        String name = doc.getString("name");
                        String photo = doc.getString("photoUrl");

                        if (name != null) userNameSmall.setText(name);

                        if (photo != null && !photo.isEmpty()) {
                            Glide.with(this).load(photo).circleCrop().into(userProfileSmall);
                        }
                    }
                });
    }

    // ---------------- LOCATION PERMISSION ----------------
    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
        } else {
            fetchCurrentLocation();
        }
    }

    // ---------------- FETCH CURRENT LOCATION ----------------
    @SuppressLint({"MissingPermission", "SetTextI18n"})
    private void fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_CODE
            );
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        getAddressFromCoordinates(location.getLatitude(), location.getLongitude());
                    } else {
                        tvLocation.setText("Unable to detect location. Move outside.");
                    }
                })
                .addOnFailureListener(e -> tvLocation.setText("Unable to detect location."));
    }

    // ---------------- COORDINATES ⇒ ADDRESS ----------------
    @SuppressLint("SetTextI18n")
    private void getAddressFromCoordinates(double lat, double lon) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        try {
            List<Address> list = geocoder.getFromLocation(lat, lon, 1);

            if (list != null && !list.isEmpty()) {
                currentAddress = list.get(0).getAddressLine(0);
                tvLocation.setText("Auto-detected: " + currentAddress);
            }
        } catch (Exception ignored) {}
    }

    // ---------------- IMAGE PICK DIALOG ----------------
    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery"};

        new AlertDialog.Builder(this)
                .setTitle("Select Image Source")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) checkCameraPermission();
                    else checkStoragePermission();
                }).show();
    }

    // ---------------- CAMERA PERMISSION ----------------
    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        } else openCamera();
    }

    // ---------------- STORAGE PERMISSION ----------------
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
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
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
            Toast.makeText(this, "Camera error: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private final ActivityResultLauncher<Intent> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            showPreviewImage(imageUri);
                        }
                    });

    // ---------------- OPEN GALLERY ----------------
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
                            showPreviewImage(imageUri);
                        }
                    });

    // ---------------- Show image in preview box ----------------
    private void showPreviewImage(Uri uri) {
        imagePreview.setVisibility(ImageView.VISIBLE);

        Glide.with(this)
                .load(uri)
                .fitCenter()
                .into(imagePreview);

        imagePreview.setOnClickListener(v -> {
            Intent intent = new Intent(report_issue.this, FullScreenImageActivity.class);
            intent.putExtra("imageUrl", uri.toString());
            startActivity(intent);
        });

        // --- CLASSIFY IMAGE AND AUTO-SELECT SPINNER ---
        isImageValid = false; // reset
        Log.d("ML_DEBUG", "showPreviewImage called, uri=" + uri);

        if (imageClassifier != null) {
            try {
                Log.d("ML_DEBUG", "about to classify");

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                ImageClassifier.Result result = imageClassifier.classify(bitmap);

                float score = result.score;      // 0..1
                String label = result.label;

                Log.d("ML_DEBUG", "label=" + label + ", score=" + score);

                // Threshold rule
                if (score < 0.6f) {
                    Toast.makeText(this,
                            "Photo not recognized. Please upload a pothole / street light / drainage photo.",
                            Toast.LENGTH_LONG).show();
                    imageUri = null;            // clear current image
                    imagePreview.setImageDrawable(null);
                    spinnerIssueType.setSelection(0);
                    isImageValid = false;
                    return;
                }

                isImageValid = true;

                String lower = label.toLowerCase();
                if (lower.contains("pothole")) {
                    spinnerIssueType.setSelection(0); // "Pot Hole"
                } else if (lower.contains("street")) {
                    spinnerIssueType.setSelection(1); // "Street Light"
                } else if (lower.contains("drain")) {
                    spinnerIssueType.setSelection(2); // "Drainage"
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ---------------- UPLOAD ISSUE ----------------
    private void uploadIssue() {
        String type = spinnerIssueType.getSelectedItem().toString();
        String desc = etDescription.getText().toString().trim();

        if (desc.isEmpty()) {
            Toast.makeText(this, "Enter description", Toast.LENGTH_SHORT).show();
            return;
        }

        // Require an image
        if (imageUri == null) {
            Toast.makeText(this,
                    "Please upload a pothole / street light / drainage photo.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // Require classifier acceptance
        if (!isImageValid) {
            Toast.makeText(this,
                    "Selected photo is not a valid issue. Upload correct photo.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading...");
        dialog.show();

        // Upload to Cloudinary in background thread
        new Thread(() -> {
            try {
                String imagePath = getImageFilePath();

                Map uploadResult = cloudinary.uploader().upload(
                        new File(imagePath),
                        ObjectUtils.asMap("folder", "samparka")
                );

                String url = uploadResult.get("secure_url").toString();

                runOnUiThread(() -> {
                    dialog.dismiss();
                    saveIssueToFirestore(type, desc, url);
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    dialog.dismiss();
                    Toast.makeText(this,
                            "Upload failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private String getImageFilePath() {
        if (cameraImageFile != null) return cameraImageFile.getAbsolutePath();
        return RealPathUtil.getRealPath(this, imageUri);
    }

    // ---------------- SAVE ISSUE ----------------
    private void saveIssueToFirestore(String type, String desc, String imageUrl) {

        String uid = null;
        try {
            uid = com.google.firebase.auth.FirebaseAuth.getInstance().getUid();
        } catch (Exception ignored) {}

        Map<String, Object> issue = new HashMap<>();
        issue.put("userId", uid);                     // who submitted the issue
        issue.put("type", type);
        issue.put("description", desc);
        issue.put("imageUrl", imageUrl);
        issue.put("address", currentAddress);
        issue.put("status", "Pending");               // default status
        issue.put("priority", "Normal");              // default priority
        issue.put("assignedTo", null);                // no authority yet
        issue.put("timestamp", System.currentTimeMillis());

        db.collection("issues")
                .add(issue)
                .addOnSuccessListener(doc -> {

                    Toast.makeText(this, "Issue Submitted!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(report_issue.this, ComplaintsActivity.class);
                    startActivity(intent);
                    finish();  // optional, so user cannot come back to form
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());

    }


    // ---------------- PERMISSION RESULT ----------------
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
