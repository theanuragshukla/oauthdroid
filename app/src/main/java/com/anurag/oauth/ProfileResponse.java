package com.anurag.oauth;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class ProfileResponse {
    @SerializedName("status")
    private boolean status;

    @SerializedName("user")
    private User user;

    @SerializedName("msg")
    private String message;

    @SerializedName("ttlImgs")
    private  int ttl;

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public ArrayList<String> getImgs() {
        return imgs;
    }

    public void setImgs(ArrayList<String> imgs) {
        this.imgs = imgs;
    }

    @SerializedName("imgs")
    private ArrayList<String> imgs;

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
