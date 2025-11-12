package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button reportIssueBtn = findViewById(R.id.btnReportIssue);
        Button myReportsBtn = findViewById(R.id.btnMyReports);
        Button helpChatBtn = findViewById(R.id.btnHelpChat);
        TextView communityUpdate = findViewById(R.id.communityUpdate);

        // ✅ Open ReportIssueActivity when the button is clicked
        reportIssueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, report_issue.class);
            startActivity(intent);
            // Optional animation between screens
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        myReportsBtn.setOnClickListener(v ->
                Toast.makeText(this, "My Reports", Toast.LENGTH_SHORT).show());

        helpChatBtn.setOnClickListener(v ->
                Toast.makeText(this, "Help & Chat", Toast.LENGTH_SHORT).show());

        communityUpdate.setOnClickListener(v ->
                Toast.makeText(this, "Panchayat Meeting Info", Toast.LENGTH_SHORT).show());
    }
}
