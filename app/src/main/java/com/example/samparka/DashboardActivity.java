package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

public class DashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        Button reportIssueBtn = findViewById(R.id.btnReportIssue);
        Button myReportsBtn = findViewById(R.id.btnMyReports);
        Button helpChatBtn = findViewById(R.id.btnHelpChat);
        TextView communityUpdate = findViewById(R.id.communityUpdate);

        // Open ReportIssueActivity when "Report New Issue" is clicked
        reportIssueBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, report_issue.class);
            startActivity(intent);
            overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        });

        // Open ComplaintsActivity when "My Reports" is clicked
        myReportsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ComplaintsActivity.class);
            startActivity(intent);
        });

        // Open HelpAssistantActivity when "Help & Chat" is clicked
        helpChatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, HelpAssistantActivity.class);
            startActivity(intent);
        });

        // Show info toast when community update is clicked
        communityUpdate.setOnClickListener(v ->
                Toast.makeText(this, "Panchayat Meeting Info", Toast.LENGTH_SHORT).show());
    }
}


