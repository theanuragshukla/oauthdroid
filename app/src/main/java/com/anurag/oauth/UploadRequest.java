package com.anurag.oauth;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UploadRequest implements Serializable {
    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    @SerializedName("img")
    private String img;
}
