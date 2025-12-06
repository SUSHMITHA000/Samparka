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

import java.util.HashMap;
import java.util.Map;

/**
 * Complaint details screen.
 * - Receives complaint fields via Intent extras (title, description, location, status, date, imageUrl, documentId)
 * - Displays image (via Glide)
 * - Loads up to two timeline entries from Firestore path: issues/{documentId}/timeline
 * - Sends a message to authority and saves to: issues/{documentId}/messages
 *
 * Note: Make sure rules allow current user to read/write these paths, or use proper auth rules.
 */
public class ComplaintDetailsActivity extends AppCompatActivity {

    ImageView imgBack, mainImage;
    TextView txtStatus, txtTitle, txtDescription, txtLocation, txtDate, txtAssigned, txtPriority;
    TextView timeline1, timeline1Date, timeline2, timeline2Date;

    Button btnSendMessage;
    EditText etMessageToAuthority;

    FirebaseFirestore db;
    FirebaseAuth auth;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaint_details);

        // ---------- INIT FIREBASE ----------
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

            String documentId = getIntent().getStringExtra("documentId");
            if (documentId == null || documentId.isEmpty()) {
                Toast.makeText(this, "Can't send: missing complaint id", Toast.LENGTH_SHORT).show();
                return;
            }

            sendMessageToAuthority(documentId, msg);
        });

        // ---------- LOAD DETAILS ----------
        loadComplaintDetails();
    }

    // ---------------- LOAD UI FROM INTENT ----------------
    @SuppressLint("SetTextI18n")
    private void loadComplaintDetails() {

        String title = safeGetStringExtra("title");
        String desc = safeGetStringExtra("description");
        String location = safeGetStringExtra("location");
        String status = safeGetStringExtra("status");
        String date = safeGetStringExtra("date");
        String imageUrl = safeGetStringExtra("imageUrl");
        String documentId = safeGetStringExtra("documentId");

        txtTitle.setText(title);
        txtDescription.setText(desc);
        txtLocation.setText(location);
        txtDate.setText(date);
        txtStatus.setText(status);
        txtAssigned.setText("Road Authority");
        txtPriority.setText("High");

        // Load image (URL or placeholder)
        if (imageUrl != null && !imageUrl.isEmpty()) {
            // using Glide (already in project)
            Glide.with(this)
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_placeholder)   // ensure this drawable exists
                    .centerCrop()
                    .into(mainImage);
        } else {
            mainImage.setImageResource(R.drawable.ic_placeholder);
        }

        // Load timeline from Firestore if we have documentId
        if (documentId != null && !documentId.isEmpty()) {
            loadTimelineFromFirestore(documentId);
        } else {
            // Fallback static values
            timeline1.setText("Complaint Submitted");
            timeline1Date.setText(date != null ? date : "");
            timeline2.setText("Assigned to Road Authority");
            timeline2Date.setText(date != null ? date : "");
        }
    }

    // Safe getter wrapper (avoids NPE)
    private String safeGetStringExtra(String key) {
        String v = getIntent().getStringExtra(key);
        return v == null ? "" : v;
    }

    // ---------------- SEND MESSAGE (SAVE TO Firestore) ----------------
    private void sendMessageToAuthority(String documentId, String messageText) {
        String uid = (auth.getCurrentUser() != null) ? auth.getCurrentUser().getUid() : "anonymous";

        Map<String, Object> msg = new HashMap<>();
        msg.put("senderId", uid);
        msg.put("text", messageText);
        msg.put("timestamp", System.currentTimeMillis());
        msg.put("read", false);

        CollectionReference messagesRef = db.collection("issues")
                .document(documentId)
                .collection("messages");

        // add message
        messagesRef.add(msg)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Message sent to authority", Toast.LENGTH_SHORT).show();
                    etMessageToAuthority.setText("");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send message: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    // ---------------- LOAD TIMELINE (first two entries) ----------------
    private void loadTimelineFromFirestore(String documentId) {
        // timeline collection under issues/{documentId}/timeline
        // We fetch latest 2 entries (ordered by timestamp desc)
        db.collection("issues")
                .document(documentId)
                .collection("timeline")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(2)
                .get()
                .addOnSuccessListener(qsnap -> {
                    if (qsnap != null && !qsnap.isEmpty()) {
                        int i = 0;
                        for (DocumentSnapshot tdoc : qsnap.getDocuments()) {
                            String event = tdoc.getString("event");
                            Long ts = tdoc.getLong("timestamp");
                            String dateStr = (ts != null) ? new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date(ts)) : "";

                            if (i == 0) {
                                timeline1.setText(event != null ? event : "Event");
                                timeline1Date.setText(dateStr);
                            } else if (i == 1) {
                                timeline2.setText(event != null ? event : "Event");
                                timeline2Date.setText(dateStr);
                            }
                            i++;
                        }
                        // if only 1 entry exists, keep second static
                        if (qsnap.size() == 1) {
                            timeline2.setText("Assigned to Road Authority");
                            timeline2Date.setText(safeGetStringExtra("date"));
                        }
                    } else {
                        // no timeline entries, fallback
                        timeline1.setText("Complaint Submitted");
                        timeline1Date.setText(safeGetStringExtra("date"));
                        timeline2.setText("Assigned to Road Authority");
                        timeline2Date.setText(safeGetStringExtra("date"));
                    }
                })
                .addOnFailureListener(e -> {
                    // failed to read timeline — use fallback values
                    timeline1.setText("Complaint Submitted");
                    timeline1Date.setText(safeGetStringExtra("date"));
                    timeline2.setText("Assigned to Road Authority");
                    timeline2Date.setText(safeGetStringExtra("date"));
                });
    }
}
