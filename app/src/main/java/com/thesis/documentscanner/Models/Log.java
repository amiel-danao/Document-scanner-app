package com.thesis.documentscanner.Models;

import com.google.firebase.firestore.DocumentReference;

import java.util.Date;

public class Log {
    private Date date;
    private DocumentReference user;
    private String message;

    public Log() {
        // Default constructor required for Firebase Realtime Database
    }

    public Log(Date date, DocumentReference user, String message) {
        this.date = date;
        this.user = user;
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public DocumentReference getUser() {
        return user;
    }

    public void setUser(DocumentReference user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
