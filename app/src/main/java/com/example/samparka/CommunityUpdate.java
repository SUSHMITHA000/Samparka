package com.example.samparka;

public class CommunityUpdate {
    private String title;
    private String message;
    private long timestamp;

    public CommunityUpdate(String title, String message, long timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public long getTimestamp() { return timestamp; }
}
