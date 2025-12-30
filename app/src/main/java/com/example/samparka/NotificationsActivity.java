package com.example.samparka;

import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private NotificationsAdapter adapter;
    private final List<NotificationModel> notifications = new ArrayList<>();
    private ListenerRegistration listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        ImageButton btnBack = findViewById(R.id.btn_back_notifications);
        btnBack.setOnClickListener(v -> finish());

        recyclerView = findViewById(R.id.notificationsRecycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificationsAdapter(notifications);
        recyclerView.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) return;

        listener = FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy("createdAt")
                .addSnapshotListener((snapshots, e) -> {
                    if (snapshots == null) return;

                    notifications.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        NotificationModel n =
                                doc.toObject(NotificationModel.class);
                        n.id = doc.getId();
                        notifications.add(n);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }
}
