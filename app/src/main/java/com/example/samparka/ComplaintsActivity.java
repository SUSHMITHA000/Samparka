package com.example.samparka;

// File: app/src/main/java/<your_package>/ComplaintsActivity.java

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ComplaintsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ComplaintAdapter adapter;
    List<Complaint> complaintList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

        recyclerView = findViewById(R.id.recyclerView);
        complaintList = new ArrayList<>();

        // Sample data (replace imageResId with your drawable resource IDs)
        complaintList.add(new Complaint(
                "Pothole on Main Road",
                "Large pothole causing traffic issues near bus stand",
                "Main Road, Near Bus Stand",
                "In Progress",
                "2025-04-10",
                R.drawable.ic_placeholder));

        complaintList.add(new Complaint(
                "Street Light Not Working",
                "Street light pole damaged, area dark at night",
                "Temple Street, Ward 5",
                "Completed",
                "2025-04-05",
                R.drawable.ic_placeholder));

        complaintList.add(new Complaint(
                "Drainage Blockage",
                "Drainage overflow during rain, causing flooding",
                "Market Area, Ward 1",
                "Pending",
                "2025-04-13",
                R.drawable.ic_placeholder));

        adapter = new ComplaintAdapter(complaintList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
}

