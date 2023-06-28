package com.anurag.oauth;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UploadResponse implements Serializable {
    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
@SerializedName("status")
    private boolean status;
    @SerializedName("msg")
    private String msg;

}
