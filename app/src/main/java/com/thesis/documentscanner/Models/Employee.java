package com.thesis.documentscanner.Models;

public class Employee {
    private String name;
    private String UID;
    private String email;
    private String avatar;
    private String role;
    private String status = "active";

    public Employee(){

    }

    public Employee(String name, String UID, String email, String avatar, String role, String status) {
        this.name = name;
        this.UID = UID;
        this.email = email;
        this.avatar = avatar;
        this.role = role;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }



    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}

