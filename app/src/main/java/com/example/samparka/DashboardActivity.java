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

        reportIssueSection = findViewById(R.id.reportIssueSection);
        btnMyReports = findViewById(R.id.btnMyReports);
        btnHelpChat = findViewById(R.id.btnHelpChat);
        communityUpdateSection = findViewById(R.id.communityUpdateSection);

        loadUserProfile(auth.getUid());
        listenForCommunityUpdates();

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

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        String photoUrl = doc.getString("photoUrl");

                        if (name != null && !name.isEmpty()) {
                            greetingText.setText("Hi " + name + " 👋");
                        } else {
                            greetingText.setText("Hi User 👋");
                        }

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this).load(photoUrl).circleCrop().into(profileIcon);
                        } else {
                            profileIcon.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }
}
