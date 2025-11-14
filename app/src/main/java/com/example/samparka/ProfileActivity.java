package com.example.samparka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Profile screen. Back button returns to Dashboard.
 */
public class ProfileActivity extends AppCompatActivity {

    private Button btnBackFromProfile;

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
    }
}
