package com.example.samparka;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class DashboardActivity extends AppCompatActivity {

    ImageView profileIcon, userProfileSmall;
    TextView userNameSmall, greetingText;

    LinearLayout profileSection, reportIssueSection;

    Button btnMyReports, btnHelpChat;  // ⭐ ADD THESE

    FirebaseAuth auth;
    FirebaseFirestore db;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // TOP BAR ITEMS
        profileIcon = findViewById(R.id.profileIcon);
        userProfileSmall = findViewById(R.id.userProfileSmall);
        userNameSmall = findViewById(R.id.userNameSmall);
        greetingText = findViewById(R.id.greetingText);

        // SECTIONS
        profileSection = findViewById(R.id.profileSection);
        reportIssueSection = findViewById(R.id.reportIssueSection);

        // ⭐ FIND BUTTONS
        btnMyReports = findViewById(R.id.btnMyReports);
        btnHelpChat = findViewById(R.id.btnHelpChat);

        loadUserProfile(auth.getUid());

        // OPEN PROFILE PAGE
        profileIcon.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class))
        );

        profileSection.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class))
        );

        // OPEN REPORT ISSUE PAGE
        reportIssueSection.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, report_issue.class))
        );

        // ⭐ OPEN MY REPORTS PAGE
        btnMyReports.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ComplaintsActivity.class))
        );

        // ⭐ OPEN HELP CHAT PAGE
        btnHelpChat.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, HelpAssistantActivity.class))
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
                            userNameSmall.setText(name);
                            greetingText.setText("Hi " + name + " 👋");
                        } else {
                            greetingText.setText("Hi User 👋");
                        }

                        if (photoUrl != null && !photoUrl.isEmpty()) {
                            Glide.with(this).load(photoUrl).circleCrop().into(profileIcon);
                            Glide.with(this).load(photoUrl).circleCrop().into(userProfileSmall);
                        } else {
                            profileIcon.setImageResource(R.drawable.ic_profile);
                            userProfileSmall.setImageResource(R.drawable.ic_profile);
                        }
                    }
                });
    }
}
