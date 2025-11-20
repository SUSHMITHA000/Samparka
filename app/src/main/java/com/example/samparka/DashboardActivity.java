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

public class DashboardActivity extends AppCompatActivity {

    ImageView profileIcon, userProfileSmall;
    TextView userNameSmall, greetingText;

    LinearLayout profileSection, reportIssueSection;

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
        userProfileSmall = findViewById(R.id.userProfileSmall);
        userNameSmall = findViewById(R.id.userNameSmall);
        greetingText = findViewById(R.id.greetingText);

        reportIssueSection = findViewById(R.id.reportIssueSection);
        profileSection = findViewById(R.id.profileSection);

        loadUserProfile(auth.getUid());

        profileIcon.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class))
        );

        profileSection.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, ProfileActivity.class))
        );

        reportIssueSection.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this, report_issue.class))
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
