package com.example.samparka;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    ImageView profileIcon, notificationIcon;
    TextView greetingText, communityUpdateText;

    // COUNT TEXTVIEWS
    TextView totalReports, inProgressReports, resolvedReports;

    // LATEST COMPLAINT VIEWS
    TextView latestComplaintStatus;
    TextView latestIssueType;
    TextView latestIssueStatus;
    TextView latestIssueLocation;
    TextView latestIssueDate;

    LinearLayout reportIssueSection, btnMyReports, btnHelpChat, communityUpdateSection;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        profileIcon = findViewById(R.id.profileIcon);
        notificationIcon = findViewById(R.id.notificationIcon);
        greetingText = findViewById(R.id.greetingText);
        communityUpdateText = findViewById(R.id.communityUpdateText);

        totalReports = findViewById(R.id.totalReports);
        inProgressReports = findViewById(R.id.inProgressReports);
        resolvedReports = findViewById(R.id.resolvedReports);

        latestComplaintStatus = findViewById(R.id.latestComplaintStatus);
        latestIssueType = findViewById(R.id.latestIssueType);
        latestIssueStatus = findViewById(R.id.latestIssueStatus);
        latestIssueLocation = findViewById(R.id.latestIssueLocation);
        latestIssueDate = findViewById(R.id.latestIssueDate);

        reportIssueSection = findViewById(R.id.reportIssueSection);
        btnMyReports = findViewById(R.id.btnMyReports);
        btnHelpChat = findViewById(R.id.btnHelpChat);
        communityUpdateSection = findViewById(R.id.communityUpdateSection);

        loadUserProfile(auth.getUid());
        listenForCommunityUpdates();
        loadComplaintCounts();
        listenForLatestComplaintStatus();
        loadLatestComplaint();

        // 🚀 SEED SAMPLE EVENTS (Run once to populate your database)
        seedSampleEvents();

        profileIcon.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class))
        );

        notificationIcon.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, NotificationsActivity.class))
        );

        reportIssueSection.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, report_issue.class))
        );

        btnMyReports.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ComplaintsActivity.class))
        );

        btnHelpChat.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, HelpAssistantActivity.class))
        );

        communityUpdateSection.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, CommunityUpdateActivity.class))
        );
    }

    private void seedSampleEvents() {
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                // Add Event 1
                Map<String, Object> event1 = new HashMap<>();
                event1.put("title", "Panchayat Meeting");
                event1.put("message", "Monthly meeting scheduled for this Sunday at 10 AM in Community Hall.");
                event1.put("timestamp", System.currentTimeMillis());
                db.collection("events").add(event1);

                // Add Event 2
                Map<String, Object> event2 = new HashMap<>();
                event2.put("title", "Blood Donation Camp");
                event2.put("message", "Join us for a blood donation camp at the Primary Health Centre on 25th Oct.");
                event2.put("timestamp", System.currentTimeMillis() - 86400000); // 1 day ago
                db.collection("events").add(event2);

                // Add Event 3
                Map<String, Object> event3 = new HashMap<>();
                event3.put("title", "New Road Construction");
                event3.put("message", "Repair work starting on Main Cross road. Please use alternative routes.");
                event3.put("timestamp", System.currentTimeMillis() - 172800000); // 2 days ago
                db.collection("events").add(event3);
            }
        });
    }

    private void listenForCommunityUpdates() {
        db.collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (communityUpdateText != null) communityUpdateText.setText("No updates available");
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        String latestEvent = value.getDocuments().get(0).getString("message");
                        if (latestEvent != null && communityUpdateText != null) {
                            communityUpdateText.setText(latestEvent);
                        }
                    } else {
                        if (communityUpdateText != null) communityUpdateText.setText("No new updates");
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void loadUserProfile(String uid) {
        if (uid == null) return;
        db.collection("users").document(uid).get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                String name = doc.getString("name");
                String photoUrl = doc.getString("photoUrl");
                greetingText.setText(name != null && !name.isEmpty() ? "Hi " + name + " 👋" : "Hi User 👋");
                if (photoUrl != null && !photoUrl.isEmpty()) {
                    Glide.with(this).load(photoUrl).circleCrop().into(profileIcon);
                } else {
                    profileIcon.setImageResource(R.drawable.ic_profile);
                }
            }
        });
    }

    private void loadComplaintCounts() {
        String uid = auth.getUid();
        if (uid == null) return;
        db.collection("issues").whereEqualTo("userId", uid).addSnapshotListener((value, error) -> {
            if (error != null || value == null) return;
            int total = 0, inProgress = 0, resolved = 0;
            for (com.google.firebase.firestore.DocumentSnapshot doc : value.getDocuments()) {
                total++;
                String status = doc.getString("status");
                if ("Completed".equalsIgnoreCase(status)) resolved++;
                else inProgress++;
            }
            if (totalReports != null) totalReports.setText(String.valueOf(total));
            if (inProgressReports != null) inProgressReports.setText(String.valueOf(inProgress));
            if (resolvedReports != null) resolvedReports.setText(String.valueOf(resolved));
        });
    }

    private void listenForLatestComplaintStatus() {
        String uid = auth.getUid();
        if (uid == null) return;
        db.collection("issues").whereEqualTo("userId", uid).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener((value, error) -> {
                    if (latestComplaintStatus == null) return;
                    if (error != null || value == null || value.isEmpty()) {
                        latestComplaintStatus.setText("Latest complaint: --");
                        return;
                    }
                    String status = value.getDocuments().get(0).getString("status");
                    latestComplaintStatus.setText("Latest complaint: " + (status != null ? status : "--"));
                });
    }

    private void loadLatestComplaint() {
        String uid = auth.getUid();
        if (uid == null) return;
        db.collection("issues").whereEqualTo("userId", uid).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener((value, error) -> {
                    if (value == null || value.isEmpty()) {
                        if (latestIssueType != null) latestIssueType.setText("No complaints yet");
                        return;
                    }
                    com.google.firebase.firestore.DocumentSnapshot doc = value.getDocuments().get(0);
                    if (latestIssueType != null) latestIssueType.setText("Issue: " + doc.getString("type"));
                    if (latestIssueLocation != null) latestIssueLocation.setText("Location: " + doc.getString("address"));
                    if (latestIssueStatus != null) latestIssueStatus.setText("Status: " + doc.getString("status"));
                    Long ts = doc.getLong("timestamp");
                    if (ts != null && latestIssueDate != null) {
                        latestIssueDate.setText("Updated on: " + new java.text.SimpleDateFormat("dd MMM yyyy").format(new java.util.Date(ts)));
                    }
                });
    }
}
