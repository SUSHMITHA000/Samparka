package com.example.samparka;

public class NotificationModel {
    public String id;
    public String title;
    public String message;
    public boolean read;
    public String complaintId;
    public com.google.firebase.Timestamp createdAt;


    public NotificationModel() {} // REQUIRED for Firestore
}
