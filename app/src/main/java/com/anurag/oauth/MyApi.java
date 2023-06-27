package com.anurag.oauth;
import java.util.Optional;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MyApi {
    @POST("/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("/signup")
    Call<SignupResponse> signup(@Body SignupRequest request);
    @GET("/profile")
    Call<ProfileResponse> getProfile(@Header("x-access-token") String accessToken);
    @GET("/token")
    Call<TokenResponse> getToken(@Query("userId") String userId, @Header("x-refresh-token") String refreshToken);
    @GET("/logout")
    Call<String> logout(@Header("x-access-token")String accessToken);
}
