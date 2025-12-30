package com.example.samparka;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ComplaintDetailsActivity extends AppCompatActivity {

    ImageView imgBack, mainImage;
    TextView txtStatus, txtTitle, txtDescription, txtLocation, txtDate, txtAssigned, txtPriority;
    TextView timeline1, timeline1Date, timeline2, timeline2Date;

    Button btnSendMessage;
    EditText etMessageToAuthority;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String complaintId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

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

        etMessageToAuthority = findViewById(R.id.etMessageToAuthority);
        btnSendMessage = findViewById(R.id.btnSendMessage);

        imgBack.setOnClickListener(v -> finish());

        // 🔑 GET COMPLAINT ID (FROM NOTIFICATION)
        complaintId = getIntent().getStringExtra("complaintId");

        if (complaintId == null || complaintId.isEmpty()) {
            Toast.makeText(this, "Invalid complaint", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Load complaint + timeline
        loadComplaintFromFirestore(complaintId);
        loadTimelineFromFirestore(complaintId);

        // SEND MESSAGE
        btnSendMessage.setOnClickListener(v -> {
            String msg = etMessageToAuthority.getText().toString().trim();
            if (msg.isEmpty()) {
                Toast.makeText(this, "Enter a message", Toast.LENGTH_SHORT).show();
                return;
            }
            sendMessageToAuthority(complaintId, msg);
        });
    }

    // ---------------- LOAD COMPLAINT ----------------
    @SuppressLint("SetTextI18n")
    private void loadComplaintFromFirestore(String complaintId) {
        db.collection("issues")
                .document(complaintId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) return;

                    txtTitle.setText(doc.getString("type"));
                    txtDescription.setText(doc.getString("description"));
                    txtLocation.setText(doc.getString("address"));
                    txtStatus.setText(doc.getString("status"));

                    txtAssigned.setText(
                            doc.getString("assignedTo") != null
                                    ? doc.getString("assignedTo")
                                    : "Unassigned"
                    );

                    txtPriority.setText(
                            doc.getString("priority") != null
                                    ? doc.getString("priority")
                                    : "Medium"
                    );

                    Long ts = doc.getLong("timestamp");
                    if (ts != null) {
                        txtDate.setText(
                                new SimpleDateFormat("dd MMM yyyy")
                                        .format(new Date(ts))
                        );
                    }

                    String imageUrl = doc.getString("imageUrl");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Glide.with(this)
                                .load(imageUrl)
                                .placeholder(R.drawable.ic_placeholder)
                                .centerCrop()
                                .into(mainImage);
                    } else {
                        mainImage.setImageResource(R.drawable.ic_placeholder);
                    }
                });
    }

    // ---------------- SEND MESSAGE ----------------
    private void sendMessageToAuthority(String documentId, String messageText) {
        String uid = auth.getCurrentUser() != null
                ? auth.getCurrentUser().getUid()
                : "anonymous";

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", uid);
        msg.put("text", messageText);
        msg.put("timestamp", System.currentTimeMillis());
        msg.put("read", false);

        CollectionReference messagesRef = db.collection("issues")
                .document(documentId)
                .collection("messages");

        messagesRef.add(msg)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
                    etMessageToAuthority.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ---------------- LOAD TIMELINE ----------------
    private void loadTimelineFromFirestore(String documentId) {
        db.collection("issues")
                .document(documentId)
                .collection("timeline")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(2)
                .get()
                .addOnSuccessListener(qsnap -> {
                    if (qsnap == null || qsnap.isEmpty()) return;

                    int i = 0;
                    for (DocumentSnapshot tdoc : qsnap.getDocuments()) {
                        String event = tdoc.getString("event");
                        Long ts = tdoc.getLong("timestamp");
                        String dateStr = ts != null
                                ? new SimpleDateFormat("dd MMM yyyy").format(new Date(ts))
                                : "";

                        if (i == 0) {
                            timeline1.setText(event);
                            timeline1Date.setText(dateStr);
                        } else {
                            timeline2.setText(event);
                            timeline2Date.setText(dateStr);
                        }
                        i++;
                    }
                });
    }
}
