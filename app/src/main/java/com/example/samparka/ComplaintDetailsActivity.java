package com.example.samparka;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ComplaintDetailsActivity extends AppCompatActivity {

    ImageView imgBack, mainImage;
    TextView txtStatus, txtTitle, txtDescription, txtLocation, txtDate, txtAssigned, txtPriority;
    TextView timeline1, timeline1Date, timeline2, timeline2Date;

    Button btnSendMessage;
    EditText etMessageToAuthority;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        // ---------- FIND VIEWS ----------
        imgBack = findViewById(R.id.imgBack);
        mainImage = findViewById(R.id.mainImage);

        txtStatus = findViewById(R.id.txtStatus);
        txtTitle = findViewById(R.id.txtTitle);
        txtDescription = findViewById(R.id.txtDescription);
        txtLocation = findViewById(R.id.txtLocation);
        txtDate = findViewById(R.id.txtDate);
        txtAssigned = findViewById(R.id.txtAssigned);
        txtPriority = findViewById(R.id.txtPriority);

        timeline1 = findViewById(R.id.timeline1);
        timeline1Date = findViewById(R.id.timeline1Date);
        timeline2 = findViewById(R.id.timeline2);
        timeline2Date = findViewById(R.id.timeline2Date);

        // message box + button
        etMessageToAuthority = findViewById(R.id.etMessageToAuthority);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        // ---------- BACK BUTTON ----------
        imgBack.setOnClickListener(v -> finish());

        // ---------- SEND MESSAGE ----------
        btnSendMessage.setOnClickListener(v -> {
            String msg = etMessageToAuthority.getText().toString().trim();

            if (msg.isEmpty()) {
                Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
                return;
            }

            // For now just show toast
            Toast.makeText(this, "Message sent to authority", Toast.LENGTH_SHORT).show();

            etMessageToAuthority.setText("");
        });

        // ---------- LOAD DETAILS ----------
        loadComplaintDetails();
    }

    @SuppressLint("SetTextI18n")
    private void loadComplaintDetails() {
        String title = getIntent().getStringExtra("title");
        String desc = getIntent().getStringExtra("description");
        String location = getIntent().getStringExtra("location");
        String status = getIntent().getStringExtra("status");
        String date = getIntent().getStringExtra("date");
        int imageResId = getIntent().getIntExtra("imageResId", R.drawable.ic_placeholder);

        mainImage.setImageResource(imageResId);
        txtTitle.setText(title);
        txtDescription.setText(desc);
        txtLocation.setText(location);
        txtDate.setText(date);
        txtAssigned.setText("Road Authority");
        txtPriority.setText("High");
        txtStatus.setText(status);

        timeline1.setText("Complaint Submitted");
        timeline1Date.setText(date);

        timeline2.setText("Assigned to Road Authority");
        timeline2Date.setText(date);
    }
}
