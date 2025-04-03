package com.example.campuseexpensemanager.model;

import java.util.Date;

public class Notification {
    private String id;
    private String title;
    private String message;
    private Date timestamp;
    private int iconResourceId;
    private boolean isRead;

    public Notification(String id, String title, String message, int iconResourceId) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.timestamp = new Date();
        this.iconResourceId = iconResourceId;
        this.isRead = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }
} 