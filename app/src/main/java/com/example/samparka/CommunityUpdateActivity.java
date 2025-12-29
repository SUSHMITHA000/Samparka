package com.example.samparka;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityUpdateActivity extends AppCompatActivity {

    private RecyclerView updateRecyclerView;
    private CommunityUpdateAdapter adapter;
    private List<CommunityUpdate> updateList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_update);

        db = FirebaseFirestore.getInstance();
        updateRecyclerView = findViewById(R.id.updateRecyclerView);
        ImageButton backButton = findViewById(R.id.backButton);

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        updateList = new ArrayList<>();
        adapter = new CommunityUpdateAdapter(updateList);
        updateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        updateRecyclerView.setAdapter(adapter);

        // 🚀 Add predefined events if the collection is empty
        seedPredefinedEvents();
        
        fetchUpdates();
    }

    private void seedPredefinedEvents() {
        db.collection("events").get().addOnSuccessListener(queryDocumentSnapshots -> {
            if (queryDocumentSnapshots.isEmpty()) {
                // Event 1
                Map<String, Object> event1 = new HashMap<>();
                event1.put("title", "Gram Sabha Meeting");
                event1.put("message", "A meeting will be held on Oct 20th at the Panchayat office to discuss road maintenance.");
                event1.put("timestamp", System.currentTimeMillis());
                db.collection("events").add(event1);

                // Event 2
                Map<String, Object> event2 = new HashMap<>();
                event2.put("title", "Drinking Water Supply Schedule");
                event2.put("message", "Water supply will be restricted on Tuesday due to maintenance at the pump house.");
                event2.put("timestamp", System.currentTimeMillis() - 3600000); // 1 hour ago
                db.collection("events").add(event2);

                // Event 3
                Map<String, Object> event3 = new HashMap<>();
                event3.put("title", "Vaccination Drive");
                event3.put("message", "Pulse Polio vaccination drive for children below 5 years on Sunday at the health center.");
                event3.put("timestamp", System.currentTimeMillis() - 7200000); // 2 hours ago
                db.collection("events").add(event3);
            }
        });
    }

    private void fetchUpdates() {
        db.collection("events")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;

                    if (value != null) {
                        updateList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            
                            // Safe timestamp handling
                            Object tsObj = doc.get("timestamp");
                            long ts = 0;
                            if (tsObj instanceof Long) ts = (Long) tsObj;
                            else if (tsObj instanceof com.google.firebase.Timestamp) ts = ((com.google.firebase.Timestamp) tsObj).toDate().getTime();

                            if (title == null) title = "Community Update";
                            if (message == null) message = "";
                            if (ts == 0) ts = System.currentTimeMillis();

                            CommunityUpdate update = new CommunityUpdate(title, message, ts);
                            updateList.add(update);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
