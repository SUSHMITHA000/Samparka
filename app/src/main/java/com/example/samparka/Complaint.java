package com.example.samparka;

public class Complaint {

    public String documentId;     // Firestore document ID
    public String title;          // Complaint type / title
    public String description;    // Complaint description
    public String location;       // Address
    public String status;         // Pending / In Progress / Completed
    public String date;           // Converted timestamp (yyyy-MM-dd)
    public String category;       // Category (Pothole, Drainage etc.)
    public String imageUrl;       // Firebase image URL
    public String assignedTo;     // Road Authority, Electricity Dept...
    public String priority;       // Normal, High, Low

    public Complaint() {
        // Required empty constructor for Firestore
    }

    public Complaint(String title, String description, String location,
                     String status, String date, int imageResId, String category) {

        this.title = title;
        this.description = description;
        this.location = location;
        this.status = status;
        this.date = date;
        this.category = category;
    }
}
