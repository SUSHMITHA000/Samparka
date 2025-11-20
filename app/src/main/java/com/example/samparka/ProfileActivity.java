package com.example.samparka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

// Added imports for logout functionality
import android.content.SharedPreferences;
import android.content.Intent;
import com.google.firebase.auth.FirebaseAuth;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Profile screen. Back button returns to Dashboard.
 */
public class ProfileActivity extends AppCompatActivity {

    private Button btnBackFromProfile;

    // Add a reference for logout button
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile); // your profile layout

        btnBackFromProfile = findViewById(R.id.saveButton);
        btnBackFromProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        // ---- LOGOUT LOGIC ----
        btnLogout = findViewById(R.id.logoutButton);
        btnLogout.setOnClickListener(v -> {
            // Clear saved session flag
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            prefs.edit().putBoolean("isLoggedIn", false).apply();

            // Sign out from Firebase, if using FirebaseAuth
            FirebaseAuth.getInstance().signOut();

            // Redirect to Login activity and clear back stack
            Intent intent = new Intent(ProfileActivity.this, Login_Page.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
