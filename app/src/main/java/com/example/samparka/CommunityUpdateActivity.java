package com.example.samparka;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CommunityUpdateActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.item_community_update);

        // Optional back button
        ImageButton backButton = findViewById(R.id.backButton);
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        /* ================= BLOOD DONATION CARD ================= */
        TextView bloodTitle = findViewById(R.id.updateTitleBlood);
        TextView bloodMessage = findViewById(R.id.updateMessageBlood);
        TextView bloodTime = findViewById(R.id.updateTimeBlood);

        if (bloodTitle != null && bloodMessage != null && bloodTime != null) {
            bloodTitle.setText("Blood Donation Camp");
            bloodMessage.setText(
                    "Blood donation camp organized at the Panchayat office. " +
                            "All healthy citizens are encouraged to participate."
            );
            bloodTime.setText("10 jan 2026 • 10:00 AM");
        }

        /* ================= PANCHAYAT MEETING CARD ================= */
        TextView meetingTitle = findViewById(R.id.updateTitleMeeting);
        TextView meetingMessage = findViewById(R.id.updateMessageMeeting);
        TextView meetingTime = findViewById(R.id.updateTimeMeeting);

        if (meetingTitle != null && meetingMessage != null && meetingTime != null) {
            meetingTitle.setText("Gram Panchayat Meeting");
            meetingMessage.setText(
                    "Monthly panchayat meeting to discuss water supply, " +
                            "road maintenance, and village development."
            );
            meetingTime.setText("31 dec 2025 • 11:30 AM");
        }
    }
}