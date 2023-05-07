package com.thesis.documentscanner.Models;
public class File {

    public File(){

    }

    public File(String fileName, String fileUrl, String userId) {
        name = fileName;
        fileurl = fileUrl;
        UserId = userId;
    }

    private String fileurl;
    private String name;

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    private String UserId;
    public String getFileurl() {
        return fileurl;
    }

    public void setFileurl(String fileurl) {
        this.fileurl = fileurl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
