package com.example.samparka;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class NotificationsActivity extends AppCompatActivity {
    private ImageButton btnBackFromNotifications;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ImageButton btnBackFromNotifications = findViewById(R.id.btn_back_notifications);

        btnBackFromNotifications.setOnClickListener(view -> finish());
    }
}
