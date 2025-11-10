package com.example.samparka;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class report_issue extends AppCompatActivity {

    Spinner spinnerIssueType;
    EditText etDescription;
    Button btnSubmit, btnVoice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);

        spinnerIssueType = findViewById(R.id.spinnerIssueType);
        etDescription = findViewById(R.id.etDescription);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnVoice = findViewById(R.id.btnVoice);

        // 🔽 Dropdown Menu Options
        String[] issueTypes = {
                "Select issue category",
                "Roads & Infrastructure",
                "Water Supply",
                "Electricity",
                "Waste Management",
                "Drainage",
                "Health & Sanitation",
                "Other"
        };

        // 🎨 Create and Set Adapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                issueTypes
        );
        spinnerIssueType.setAdapter(adapter);

        // 🧾 Submit Button Action
        btnSubmit.setOnClickListener(v -> {
            String selectedIssue = spinnerIssueType.getSelectedItem().toString();
            String description = etDescription.getText().toString().trim();

            if (selectedIssue.equals("Select issue category")) {
                Toast.makeText(this, "Please select an issue type", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) {
                Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show();
                return;
            }

            // Simulate submission (Later connect to Firebase)
            Toast.makeText(this, "Complaint submitted for: " + selectedIssue, Toast.LENGTH_LONG).show();
        });
    }
}
