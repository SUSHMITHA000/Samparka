package com.example.samparka;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profilePhoto, backButton;
    private Button changePhotoButton, saveButton, helpButton, logoutButton;
    private EditText fullNameEdit, phoneEdit, emailEdit, pinEdit, cityEdit, stateEdit, locationEdit;

    FirebaseAuth auth;
    FirebaseFirestore db;

    Uri imageUri = null;
    String uploadedPhotoUrl = null;

    Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
            "cloud_name", "dvxnedfzr",
            "api_key", "939937542917265",
            "api_secret", "swOxStKjCgXcjh6AaGcvprTVoH0"
    ));

    private static final int STORAGE_PERMISSION = 100;

    private final ActivityResultLauncher<Intent> galleryLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    imageUri = result.getData().getData();
                    Glide.with(this).load(imageUri).into(profilePhoto);
                    uploadPhotoToCloudinary();
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        backButton = findViewById(R.id.backButton);
        profilePhoto = findViewById(R.id.profilePhoto);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        fullNameEdit = findViewById(R.id.fullNameEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
        emailEdit = findViewById(R.id.emailEdit);
        pinEdit = findViewById(R.id.pinEdit);
        cityEdit = findViewById(R.id.cityEdit);
        stateEdit = findViewById(R.id.stateEdit);
        locationEdit = findViewById(R.id.locationEdit);
        saveButton = findViewById(R.id.saveButton);
        helpButton = findViewById(R.id.helpButton);
        logoutButton = findViewById(R.id.logoutButton);

        loadUserData();

        backButton.setOnClickListener(v -> finish());
        changePhotoButton.setOnClickListener(v -> requestStoragePermission());
        saveButton.setOnClickListener(v -> saveProfileChanges());

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(ProfileActivity.this, Login_Page.class));
            finish();
        });

        pinEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String pin = s.toString().trim();
                if (pin.length() == 6) fetchAddressFromPincode(pin);
            }
        });
    }

    private void loadUserData() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        String name = doc.getString("name");
                        String email = doc.getString("email");
                        String phone = doc.getString("phone");
                        String pin = doc.getString("pin");
                        String city = doc.getString("city");
                        String state = doc.getString("state");
                        String location = doc.getString("location");
                        String photoUrl = doc.getString("photoUrl");

                        if (name != null) fullNameEdit.setText(name);
                        if (email != null) emailEdit.setText(email);
                        if (phone != null) phoneEdit.setText(phone);
                        if (pin != null) pinEdit.setText(pin);
                        if (city != null) cityEdit.setText(city);
                        if (state != null) stateEdit.setText(state);
                        if (location != null) locationEdit.setText(location);

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            uploadedPhotoUrl = photoUrl;
                            Glide.with(this).load(photoUrl).into(profilePhoto);
                        }
                    }
                });
    }

    private void saveProfileChanges() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", fullNameEdit.getText().toString().trim());
        updates.put("email", emailEdit.getText().toString().trim());
        updates.put("phone", phoneEdit.getText().toString().trim());
        updates.put("pin", pinEdit.getText().toString().trim());
        updates.put("city", cityEdit.getText().toString().trim());
        updates.put("state", stateEdit.getText().toString().trim());
        updates.put("location", locationEdit.getText().toString().trim());
        updates.put("photoUrl", uploadedPhotoUrl);

        db.collection("users").document(user.getUid())
                .update(updates)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show()
                )
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        STORAGE_PERMISSION);
            } else openGallery();
        } else {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION);
            } else openGallery();
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void uploadPhotoToCloudinary() {
        new Thread(() -> {
            try {
                String realPath = RealPathUtil.getRealPath(this, imageUri);
                File file = new File(realPath);

                Map uploadResult = cloudinary.uploader().upload(
                        file,
                        ObjectUtils.asMap("folder", "samparka/profile_photos")
                );

                uploadedPhotoUrl = uploadResult.get("secure_url").toString().trim();

                runOnUiThread(() ->
                        Toast.makeText(this, "Photo updated!", Toast.LENGTH_SHORT).show()
                );

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_LONG).show()
                );
            }
        }).start();
    }

    private void fetchAddressFromPincode(String pin) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(ProfileActivity.this, Locale.getDefault());
                List<Address> list = geocoder.getFromLocationName(pin, 5);

                if (list != null && !list.isEmpty()) {
                    Address a = list.get(0);

                    final String fCity = a.getLocality() != null ? a.getLocality() : a.getSubAdminArea();
                    final String fState = a.getAdminArea();
                    final String fAddress = a.getAddressLine(0);

                    runOnUiThread(() -> {
                        if (fCity != null) cityEdit.setText(fCity);
                        if (fState != null) stateEdit.setText(fState);

                        if (fAddress != null &&
                                locationEdit.getText().toString().trim().isEmpty()) {
                            locationEdit.setText(fAddress);
                        }
                    });
                }
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(ProfileActivity.this,
                                "PIN lookup failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == STORAGE_PERMISSION &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            openGallery();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }
}
