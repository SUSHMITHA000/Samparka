package com.example.samparka;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    ImageView profileIcon;
    TextView greetingText;

    LinearLayout reportIssueSection, btnMyReports, btnHelpChat, btnEvents, communityUpdateSection;

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

        reportIssueSection = findViewById(R.id.reportIssueSection);
        btnMyReports = findViewById(R.id.btnMyReports);
        btnHelpChat = findViewById(R.id.btnHelpChat);
        btnEvents = findViewById(R.id.btnEvents);
        communityUpdateSection = findViewById(R.id.communityUpdateSection);

        loadUserProfile(auth.getUid());

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

        btnEvents.setOnClickListener(v ->
                Toast.makeText(this, "Upcoming Events coming soon!", Toast.LENGTH_SHORT).show()
        );

        communityUpdateSection.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, NotificationsActivity.class))
        );
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
