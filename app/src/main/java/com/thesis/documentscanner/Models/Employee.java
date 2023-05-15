package com.thesis.documentscanner.Models;

public class Employee {
    private String name;
    private String UID;
    private String email;
    private String avatar;
    private String role;

    public Employee(){

    }

    public Employee(String name, String avatar, String role, String UID) {
        this.name = name;
        this.UID = UID;
        this.avatar = avatar;
        this.role = role;
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

