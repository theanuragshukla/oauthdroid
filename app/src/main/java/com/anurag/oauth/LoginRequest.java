package com.anurag.oauth;

public class LoginRequest {
    private String username;
    private String password;

    LoginRequest(String username, String password){
        this.password = password;
        this.username = username;
    }
}
