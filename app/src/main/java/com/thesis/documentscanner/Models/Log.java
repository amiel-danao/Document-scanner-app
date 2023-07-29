package com.thesis.documentscanner.Models;

import java.util.Date;

public class Log {
    private Date date;
    private String user;
    private String uid;
    private String message;

    public Log() {
        // Default constructor required for Firebase Realtime Database
    }

    public Log(Date date, String user, String message, String uid) {
        this.date = date;
        this.user = user;
        this.message = message;
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
