package com.example.samparka;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;



import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ComplaintsActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    ComplaintAdapter adapter;

    List<Complaint> complaintList, filteredList;
    Button btnAll, btnPending, btnProgress, btnDone;
    Spinner spinnerCategory;
    ImageView imgFilterIcon;

    String[] categories = {"All Categories", "Roads", "Water", "Electricity", "Waste", "Drainage"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complaints);

//        setContentView(R.layout.activity_complaints);

        recyclerView = findViewById(R.id.recyclerView);
        btnAll = findViewById(R.id.btnAll);
        btnPending = findViewById(R.id.btnPending);

        btnProgress = findViewById(R.id.btnProgress);
        btnDone = findViewById(R.id.btnDone);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        imgFilterIcon = findViewById(R.id.imgFilterIcon);

        complaintList = new ArrayList<>();
        filteredList = new ArrayList<>();

        // Example complaints, add category property in Complaint class if needed
        complaintList.add(new Complaint(
                "Pothole on Main Road",
                "Large pothole causing traffic issues near bus stand",
                "Main Road, Near Bus Stand",
                "In Progress",
                "2025-04-10",
                R.drawable.ic_placeholder,
                "Roads"
        ));

        complaintList.add(new Complaint(
                "Street Light Not Working",
                "Street light pole damaged, area dark at night",
                "Temple Street, Ward 5",
                "Completed",
                "2025-04-05",
                R.drawable.ic_placeholder,
                "Electricity"
        ));

        complaintList.add(new Complaint(
                "Drainage Blockage",
                "Drainage overflow during rain, causing flooding",
                "Market Area, Ward 1",
                "Pending",
                "2025-04-13",
                R.drawable.ic_placeholder,
                "Drainage"
        ));

        adapter = new ComplaintAdapter(complaintList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(spinnerAdapter);

        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCategory = categories[position];
                filterByCategory(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        btnAll.setOnClickListener(v -> adapter.updateList(complaintList));
        btnPending.setOnClickListener(v -> filterComplaints("Pending"));
        btnProgress.setOnClickListener(v -> filterComplaints("In Progress"));
        btnDone.setOnClickListener(v -> filterComplaints("Completed"));
    }

    private void filterComplaints(String status) {
        filteredList.clear();
        for (Complaint c : complaintList) {
            if (c.status.equalsIgnoreCase(status) ||
                    (status.equals("In Progress") && c.status.equalsIgnoreCase("Progress")) ||
                    (status.equals("Completed") && c.status.equalsIgnoreCase("Done"))) {
                filteredList.add(c);
            }
        }
        adapter.updateList(new ArrayList<>(filteredList));
    }

    private void filterByCategory(String category) {
        if (category.equals("All Categories")) {
            adapter.updateList(complaintList);
        } else {
            filteredList.clear();
            for (Complaint c : complaintList) {
                if (c.category.equalsIgnoreCase(category)) {
                    filteredList.add(c);
                }
            }
            adapter.updateList(new ArrayList<>(filteredList));
        }
    }
}
