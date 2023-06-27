package com.anurag.oauth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    void loginRequired(){
        Toast.makeText(MainActivity.this, "Login required", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
    SharedPreferences prefs;
    final MyApi myApi = APIClient.getClient().create(MyApi.class);

    EditText username;
    EditText email;
    MaterialTextView greet;
    public void fetchToken(){
        String userId = prefs.getString("userId", null);
        String refreshToken = prefs.getString("refreshToken", null);
        if(userId==null || refreshToken==null){
            loginRequired();
            return;
        }
        Call<TokenResponse> call = myApi.getToken(userId, refreshToken);
        call.enqueue(new Callback<TokenResponse>() {
            @Override
            public void onResponse(Call<TokenResponse> call, Response<TokenResponse> response) {
                TokenResponse tres = response.body();
                if (tres.isStatus()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("userId", tres.getUser().getUid());
                    editor.putString("accessToken", tres.getAccessToken());
                    editor.putString("refreshToken", tres.getRefreshToken());
                    editor.apply();
                    fetchUserProfile();
                } else {
                    Toast.makeText(MainActivity.this, tres.getMsg(), Toast.LENGTH_SHORT).show();
                    loginRequired();
                }
            }

            @Override
            public void onFailure(Call<TokenResponse> call, Throwable t) {
                loginRequired();
            }
        });
    }

    public void fetchUserProfile(){
        String accessToken = prefs.getString("accessToken", null);
        if(accessToken==null){
            loginRequired();
            return;
        }else{
            Call<ProfileResponse> call = myApi.getProfile(accessToken);
            call.enqueue(new Callback<ProfileResponse>() {
                @Override
                public void onResponse(Call<ProfileResponse> call, Response<ProfileResponse> response) {
                    ProfileResponse res = response.body();
                    if(res.isStatus()){
                        Toast.makeText(MainActivity.this, "welcome back", Toast.LENGTH_SHORT).show();
                        User user = res.getUser();
                    greet.setText("Welcome "+user.getFirstName());
                    email.setText(user.getEmail());
                    username.setText(user.getUsername());
                    }else{
                        Toast.makeText(MainActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                           fetchToken();
                    }
                }
                @Override
                public void onFailure(Call<ProfileResponse> call, Throwable t) {
                    loginRequired();
                }
            });
        }
    }
    void logout(){
        String accessToken = prefs.getString("accessToken", null);
        Call<String> c = myApi.logout(accessToken);
        c.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Toast.makeText(MainActivity.this, "Logout Successful", Toast.LENGTH_SHORT).show();
            }
            @Override
            public void onFailure(Call call, Throwable t) {

            }
        });
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        loginRequired();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("super_secret", MODE_PRIVATE);
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        greet = findViewById(R.id.greeting);
        if(getIntent().hasExtra("data")){
            User res = (User) getIntent().getSerializableExtra("data");
            greet.setText("Welcome "+res.getFirstName());
            email.setText(res.getEmail());
            username.setText(res.getUsername());
        }else{
            fetchUserProfile();
        }
        MaterialButton logoutBtn = findViewById(R.id.logoutButton);
        logoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }
}
