package com.example.samparka;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Notifications screen. Simple Back button.
 */
public class NotificationsActivity extends AppCompatActivity {

    private Button btnBackFromNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications); // your layout

        btnBackFromNotifications = findViewById(R.id.btn_back_notifications);
        btnBackFromNotifications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish(); // go back to previous activity (Dashboard)
            }
        });
    }
}
