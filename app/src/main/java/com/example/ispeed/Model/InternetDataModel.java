package com.example.ispeed.Model;

import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class InternetDataModel {
    String user_id,location,uploadSpeed,downLoadSpeed,isp,ping;
    private @ServerTimestamp Date time;
    boolean stability;


    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(String uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    public String getDownLoadSpeed() {
        return downLoadSpeed;
    }

    public void setDownLoadSpeed(String downLoadSpeed) {
        this.downLoadSpeed = downLoadSpeed;
    }

    public String getIsp() {
        return isp;
    }

    public void setIsp(String isp) {
        this.isp = isp;
    }

    public String getPing() {
        return ping;
    }

    public void setPing(String ping) {
        this.ping = ping;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public boolean isStability() {
        return stability;
    }

    public void setStability(boolean stability) {
        this.stability = stability;
    }
}
