package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class RoadAuthorityDashboardActivity extends AppCompatActivity {

    ImageView ivNotification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard_officer);

        ivNotification = findViewById(R.id.ivNotification);

        ivNotification.setOnClickListener(v -> {
            Intent intent = new Intent(RoadAuthorityDashboardActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });
    }
}
