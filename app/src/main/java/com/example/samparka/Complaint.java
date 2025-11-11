package com.example.samparka;

public class Complaint {
    public String title;
    public String description;
    public String location;
    public String status;
    public String date;
    public int imageResId;

    public Complaint(String title, String description, String location, String status, String date, int imageResId) {
        this.title = title;
        this.description = description;
        this.location = location;
        this.status = status;
        this.date = date;
        this.imageResId = imageResId;
    }
}

