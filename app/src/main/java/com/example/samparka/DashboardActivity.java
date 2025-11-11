//package com.example.samparka;
//
//import android.os.Bundle;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//import androidx.appcompat.app.AppCompatActivity;
//
//public class DashboardActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_dashboard);
//
//        Button reportIssueBtn = findViewById(R.id.btnReportIssue);
//        Button myReportsBtn = findViewById(R.id.btnMyReports);
//        Button helpChatBtn = findViewById(R.id.btnHelpChat);
//        TextView communityUpdate = findViewById(R.id.communityUpdate);
//
//        reportIssueBtn.setOnClickListener(v ->
//                Toast.makeText(this, "Report New Issue clicked", Toast.LENGTH_SHORT).show());
//
//        myReportsBtn.setOnClickListener(v ->
//                Toast.makeText(this, "My Reports", Toast.LENGTH_SHORT).show());
//
//        helpChatBtn.setOnClickListener(v ->
//                Toast.makeText(this, "Help & Chat", Toast.LENGTH_SHORT).show());
//
//        communityUpdate.setOnClickListener(v ->
//                Toast.makeText(this, "Panchayat Meeting Info", Toast.LENGTH_SHORT).show());
//    }
//}

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

        reportIssueBtn.setOnClickListener(v ->
                Toast.makeText(this, "Report New Issue clicked", Toast.LENGTH_SHORT).show());

        // Updated functionality: Open ComplaintsActivity when "My Reports" is clicked
        myReportsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ComplaintsActivity.class);
            startActivity(intent);
        });

        helpChatBtn.setOnClickListener(v ->
                Toast.makeText(this, "Help & Chat", Toast.LENGTH_SHORT).show());

        communityUpdate.setOnClickListener(v ->
                Toast.makeText(this, "Panchayat Meeting Info", Toast.LENGTH_SHORT).show());
    }
}

