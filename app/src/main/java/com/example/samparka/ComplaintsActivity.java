package com.example.samparka;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ComplaintsActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    ComplaintAdapter adapter;

    List<Complaint> complaintList, filteredList;

    Button btnAll, btnPending, btnProgress, btnDone;
    Spinner spinnerCategory;
    ImageView imgBack, imgFilterIcon;

    FirebaseFirestore db;
    FirebaseAuth auth;

    String[] categories = {
            "All Categories",
            "Street Light",
            "Pothole",
            "Drainage"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Views
        recyclerView = findViewById(R.id.recyclerView);
        btnAll = findViewById(R.id.btnAll);
        btnPending = findViewById(R.id.btnPending);
        btnProgress = findViewById(R.id.btnProgress);
        btnDone = findViewById(R.id.btnDone);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imgFilterIcon = findViewById(R.id.imgFilterIcon);

        // Lists
        complaintList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Adapter
        adapter = new ComplaintAdapter(ComplaintsActivity.this, complaintList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // Load Firestore data
        loadUserComplaints();

        // Spinner setup
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                filterByCategory(categories[pos]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // Filter buttons
        btnAll.setOnClickListener(v -> adapter.updateList(complaintList));
        btnPending.setOnClickListener(v -> filterComplaints("Pending"));
        btnProgress.setOnClickListener(v -> filterComplaints("In Progress"));
        btnDone.setOnClickListener(v -> filterComplaints("Completed"));
    }

    // ----------------- LOAD COMPLAINTS FROM FIRESTORE -------------------
    private void loadUserComplaints() {
        String uid = auth.getUid();
        if (uid == null) return;

        db.collection("issues")
                .whereEqualTo("userId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {

                    if (error != null || value == null) {
                        Toast.makeText(this, "Error loading complaints!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    complaintList.clear();

                    for (DocumentSnapshot doc : value.getDocuments()) {

                        String title = doc.getString("type");
                        String desc = doc.getString("description");
                        String address = doc.getString("address");
                        String status = doc.getString("status");
                        Long ts = doc.getLong("timestamp");
                        String date = convertTimestamp(ts);
                        String imageUrl = doc.getString("imageUrl");

                        Complaint c = new Complaint(
                                title,
                                desc,
                                address,
                                status,
                                date,
                                R.drawable.ic_placeholder,
                                title  // Type = category
                        );

                        c.imageUrl = imageUrl;
                        c.documentId = doc.getId();

                        complaintList.add(c);
                    }

                    adapter.updateList(complaintList);
                });
    }

    // ----------------- TIMESTAMP → DATE -------------------
    private String convertTimestamp(Long ts) {
        if (ts == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date(ts));
    }

    // ----------------- FILTER BY STATUS -------------------
    private void filterComplaints(String status) {
        filteredList.clear();

        for (Complaint c : complaintList) {
            if (c.status.equalsIgnoreCase(status)) {
                filteredList.add(c);
            }
        }

        adapter.updateList(filteredList);
    }

    // ----------------- FILTER BY CATEGORY -------------------
    private void filterByCategory(String category) {
        if (category.equals("All Categories")) {
            adapter.updateList(complaintList);
            return;
        }

        filteredList.clear();

        for (Complaint c : complaintList) {
            if (c.category.equalsIgnoreCase(category)) {
                filteredList.add(c);
            }
        }

        adapter.updateList(filteredList);
    }
}
