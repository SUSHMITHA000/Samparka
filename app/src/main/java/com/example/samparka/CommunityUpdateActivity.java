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
import java.util.List;

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

        fetchUpdates();
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
                            Long ts = doc.getLong("timestamp");

                            // Use defaults if fields are missing to avoid crash
                            if (title == null) title = "Community Update";
                            if (message == null) message = "";
                            long finalTs = (ts != null) ? ts : System.currentTimeMillis();

                            CommunityUpdate update = new CommunityUpdate(title, message, finalTs);
                            updateList.add(update);
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
