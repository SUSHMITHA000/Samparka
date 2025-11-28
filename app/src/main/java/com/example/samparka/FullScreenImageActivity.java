package com.example.samparka;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class FullScreenImageActivity extends AppCompatActivity {

    ImageView fullImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        fullImageView = findViewById(R.id.fullImageView);

        String imageUrl = getIntent().getStringExtra("imageUrl");

        Glide.with(this)
                .load(imageUrl)
                .fitCenter()
                .into(fullImageView);

        // Close when clicked
        fullImageView.setOnClickListener(v -> finish());
    }
}
