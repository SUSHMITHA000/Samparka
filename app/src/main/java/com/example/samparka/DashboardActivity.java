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

public class DashboardActivity extends AppCompatActivity {

    ImageView profileIcon;
    TextView greetingText, communityUpdateText;

    // ✅ COUNT TEXTVIEWS
    TextView totalReports, inProgressReports, resolvedReports;

    // ✅ LATEST COMPLAINT VIEWS
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
        greetingText = findViewById(R.id.greetingText);
        communityUpdateText = findViewById(R.id.communityUpdateText);

        // ✅ INIT COUNT TEXTVIEWS
        totalReports = findViewById(R.id.totalReports);
        inProgressReports = findViewById(R.id.inProgressReports);
        resolvedReports = findViewById(R.id.resolvedReports);

        // ✅ INIT LATEST COMPLAINT TEXTVIEWS (THIS WAS MISSING)
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

        // ✅ DATA LOADERS
        loadComplaintCounts();
        listenForLatestComplaintStatus();
        loadLatestComplaint();

        profileIcon.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class))
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

    // ---------------- COMMUNITY UPDATES ----------------
    private void listenForCommunityUpdates() {
        db.collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        communityUpdateText.setText("No updates available");
                        return;
                    }

                    if (value != null && !value.isEmpty()) {
                        String latestEvent = value.getDocuments().get(0).getString("message");
                        communityUpdateText.setText(latestEvent != null ? latestEvent : "No updates");
                    } else {
                        communityUpdateText.setText("No new updates");
                    }
                });
    }

    // ---------------- USER PROFILE ----------------
    @SuppressLint("SetTextI18n")
    private void loadUserProfile(String uid) {
        if (uid == null) return;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String photoUrl = doc.getString("photoUrl");

                        greetingText.setText(
                                name != null && !name.isEmpty()
                                        ? "Hi " + name + " 👋"
                                        : "Hi User 👋"
                        );

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this).load(photoUrl).circleCrop().into(profileIcon);
                        } else {
                            profileIcon.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }

    // ---------------- COMPLAINT COUNTS ----------------
    private void loadComplaintCounts() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("issues")
                .whereEqualTo("userId", uid)
                .addSnapshotListener((value, error) -> {
                    if (error != null || value == null) return;

                    int total = 0;
                    int inProgress = 0;
                    int resolved = 0;

                    for (var doc : value.getDocuments()) {
                        total++;
                        String status = doc.getString("status");

                        if ("Completed".equalsIgnoreCase(status)) {
                            resolved++;
                        } else {
                            inProgress++; // Pending + In Progress
                        }
                    }

                    totalReports.setText(String.valueOf(total));
                    inProgressReports.setText(String.valueOf(inProgress));
                    resolvedReports.setText(String.valueOf(resolved));
                });
    }

    // ---------------- LATEST COMPLAINT STATUS ----------------
    private void listenForLatestComplaintStatus() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("issues")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {

                    if (latestComplaintStatus == null) return;

                    if (error != null || value == null || value.isEmpty()) {
                        latestComplaintStatus.setText("Latest complaint: --");
                        return;
                    }

                    String status = value.getDocuments().get(0).getString("status");
                    latestComplaintStatus.setText(
                            "Latest complaint: " + (status != null ? status : "--")
                    );
                });
    }

    // ---------------- FULL LATEST COMPLAINT DETAILS ----------------
    private void loadLatestComplaint() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("issues")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((value, error) -> {

                    if (value == null || value.isEmpty()) {
                        latestIssueType.setText("No complaints yet");
                        latestIssueLocation.setText("");
                        latestIssueStatus.setText("");
                        latestIssueDate.setText("");
                        return;
                    }

                    var doc = value.getDocuments().get(0);

                    latestIssueType.setText("Issue: " + doc.getString("type"));
                    latestIssueLocation.setText("Location: " + doc.getString("address"));
                    latestIssueStatus.setText("Status: " + doc.getString("status"));

                    Long ts = doc.getLong("timestamp");
                    if (ts != null) {
                        java.text.SimpleDateFormat sdf =
                                new java.text.SimpleDateFormat("dd MMM yyyy");
                        latestIssueDate.setText("Updated on: " +
                                sdf.format(new java.util.Date(ts)));
                    }
                });
    }
}
