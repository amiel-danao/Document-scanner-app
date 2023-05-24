package com.thesis.documentscanner.Models;

import androidx.annotation.Nullable;

import com.google.firebase.Timestamp;
import com.thesis.documentscanner.util.Status;

public class File {

    private String docId;
    private String fileurl;
    private String qrUrl;
    private String name;
    private String fileType;
    private String visibility;
    private String sender;
    private Timestamp dateUploaded;
    private String status = Status.PENDING.name();

    public File(){

    }

    public File(String docId, String fileurl, String qrUrl, String name, String fileType, String visibility, String sender, Timestamp dateUploaded, String userId) {
        this.docId = docId;
        this.fileurl = fileurl;
        this.qrUrl = qrUrl;
        this.name = name;
        this.fileType = fileType;
        this.visibility = visibility;
        this.sender = sender;
        this.dateUploaded = dateUploaded;
        UserId = userId;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Timestamp getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(Timestamp dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

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

    public String getQrUrl() {
        return qrUrl;
    }

    public void setQrUrl(String qrUrl) {
        this.qrUrl = qrUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        assert obj != null;
        if (getClass() != obj.getClass())
            return false;
        File otherFile = (File)obj;
        return this.fileurl.equals(otherFile.fileurl);
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }
}
