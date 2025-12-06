package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private static final int SPLASH_DELAY = 2500; // 2.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load animation
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Apply animation to logo
        ImageView logo = findViewById(R.id.logo);
        logo.startAnimation(fadeIn);

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // User is logged in, go to Dashboard
                    startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                } else {
                    // User is not logged in, go to Login Page
                    startActivity(new Intent(MainActivity.this, Login_Page.class));
                }
                finish(); // In either case, finish this activity
            }
        }, SPLASH_DELAY);
    }
}
