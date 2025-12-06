package com.example.samparka;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private EditText fullNameEdit, phoneEdit, pinEdit, cityEdit, stateEdit, locationEdit;
    private ImageView profilePhoto, backButton;
    private Button saveButton, changePhotoButton, logoutButton;

    FirebaseAuth auth;
    FirebaseFirestore db;

    String profileImageUrl = "";

    // Image picker launcher
    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    uploadImageToCloudinary(imageUri);
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        fullNameEdit = findViewById(R.id.fullNameEdit);
        phoneEdit = findViewById(R.id.phoneEdit);
        pinEdit = findViewById(R.id.pinEdit);
        cityEdit = findViewById(R.id.cityEdit);
        stateEdit = findViewById(R.id.stateEdit);
        locationEdit = findViewById(R.id.locationEdit);
        profilePhoto = findViewById(R.id.profilePhoto);

        changePhotoButton = findViewById(R.id.changePhotoButton);
        saveButton = findViewById(R.id.saveButton);
        backButton = findViewById(R.id.backButton);
        logoutButton = findViewById(R.id.logoutButton);

        loadUserData(auth.getUid());

        pinEdit.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String pincode = pinEdit.getText().toString();
                if (pincode.length() == 6) fetchPinDetails(pincode);
            }
        });

        saveButton.setOnClickListener(v -> saveUserData());
        backButton.setOnClickListener(v -> finish());

        changePhotoButton.setOnClickListener(v -> pickImage());

        logoutButton.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(ProfileActivity.this, Login_Page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    // LOAD DATA FROM FIRESTORE
    private void loadUserData(String uid) {
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {

                        fullNameEdit.setText(doc.getString("name"));
                        phoneEdit.setText(doc.getString("phone"));
                        pinEdit.setText(doc.getString("pin"));
                        cityEdit.setText(doc.getString("city"));
                        stateEdit.setText(doc.getString("state"));
                        locationEdit.setText(doc.getString("location"));
                        profileImageUrl = doc.getString("photoUrl");

                        if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                            Glide.with(this).load(profileImageUrl).into(profilePhoto);
                        }
                    }
                });
    }

    // SAVE DATA TO FIRESTORE
    private void saveUserData() {

        String uid = auth.getUid();
        if (uid == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("name", fullNameEdit.getText().toString());
        data.put("phone", phoneEdit.getText().toString());
        data.put("pin", pinEdit.getText().toString());
        data.put("city", cityEdit.getText().toString());
        data.put("state", stateEdit.getText().toString());
        data.put("location", locationEdit.getText().toString());
        data.put("photoUrl", profileImageUrl);

        db.collection("users")
                .document(uid)
                .update(data)
                .addOnSuccessListener(a -> Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // PICK IMAGE
    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    // UPLOAD IMAGE TO CLOUDINARY
    private void uploadImageToCloudinary(Uri imageUri) {

        try {
            InputStream inputStream = getContentResolver().openInputStream(imageUri);

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            byte[] imageData = baos.toByteArray();

            Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", "dvxnedfzr",
                    "api_key", "939937542917265",
                    "api_secret", "swOxStKjCgXcjh6AaGcvprTVoH0s"
            ));

            new Thread(() -> {
                try {
                    Map result = cloudinary.uploader().upload(imageData,
                            ObjectUtils.asMap("folder", "samparka/user_profiles"));

                    profileImageUrl = result.get("secure_url").toString();

                    runOnUiThread(() -> {
                        Glide.with(this).load(profileImageUrl).into(profilePhoto);
                        Toast.makeText(this, "Photo Updated!", Toast.LENGTH_SHORT).show();
                    });

                } catch (Exception e) {
                    runOnUiThread(() ->
                            Toast.makeText(this, "Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            }).start();

        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // PIN LOOKUP API
    private void fetchPinDetails(String pincode) {

        new Thread(() -> {
            try {
                URL url = new URL("https://api.postalpincode.in/pincode/" + pincode);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                InputStream in = conn.getInputStream();
                ByteArrayOutputStream out = new ByteArrayOutputStream();

                int read;
                byte[] buffer = new byte[1024];

                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }

                String response = out.toString();

                JSONArray array = new JSONArray(response);
                JSONObject obj = array.getJSONObject(0);

                if (obj.getString("Status").equals("Success")) {

                    JSONObject postOffice =
                            obj.getJSONArray("PostOffice").getJSONObject(0);

                    String district = postOffice.getString("District");
                    String state = postOffice.getString("State");

                    runOnUiThread(() -> {
                        cityEdit.setText(district);
                        stateEdit.setText(state);
                    });
                }

            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Invalid Pincode", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
