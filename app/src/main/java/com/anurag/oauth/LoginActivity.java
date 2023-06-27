package com.anurag.oauth;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.anurag.oauth.utils.TextValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    Map<Integer, Boolean> errors = new HashMap<>();

    SharedPreferences prefs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("super_secret", MODE_PRIVATE);
        setContentView(R.layout.activity_login);
        MaterialTextView signupButton = findViewById(R.id.redirectToSignup);
        signupButton.setOnClickListener(v -> startActivity(new Intent(LoginActivity.this, SignupActivity.class)));
        EditText usernameField = findViewById(R.id.username);
        TextInputLayout usernameLayout = findViewById(R.id.username_layout);
        EditText passwordField = findViewById(R.id.password);
        TextInputLayout passwordLayout = findViewById(R.id.password_layout);

        usernameField.addTextChangedListener(new TextValidator(usernameField) {
            @Override
            public void validate(TextView textView, String text) {
                if(text.length()<3){
                    usernameLayout.setError("Enter a valid Username");
                    errors.put(R.id.username, true);
                }else{
                    usernameLayout.setError(null);
                    errors.put(R.id.username, false);
                }
            }
        });
        passwordField.addTextChangedListener(new TextValidator(passwordField) {
            @Override
            public void validate(TextView textView, String text) {
                String errorMsg = null;
                if(text.length()==0){
                    errorMsg = "Password can't be empty";
                    errors.put(R.id.password, true);
                }else{
                    errors.put(R.id.password, false);
                }
                passwordLayout.setError(errorMsg);
            }
        });

        MaterialButton loginButton = (MaterialButton) findViewById(R.id.loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(errors.getOrDefault(R.id.username, true)){
                    Toast.makeText(LoginActivity.this, "Enter a valid Username", Toast.LENGTH_SHORT).show();
                    return;
                }else if(errors.getOrDefault(R.id.password, true)){
                    Toast.makeText(LoginActivity.this, "Enter a valid Password", Toast.LENGTH_SHORT).show();
                    return;
                }
                LoginRequest request = new LoginRequest(usernameField.getText().toString(), passwordField.getText().toString());
                MyApi myApi = APIClient.getClient().create(MyApi.class);
                Call<LoginResponse> call = myApi.login(request);
                call.enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        LoginResponse res = response.body();
                        if(res.isStatus()){
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("userId", res.getUser().getUid());
                            editor.putString("accessToken", res.getAccessToken());
                            editor.putString("refreshToken", res.getRefreshToken());
                            editor.apply();
                            Intent it = new Intent(LoginActivity.this, MainActivity.class);
                            it.putExtra("data", res.getUser());
                            startActivity(it);
                        }else{
                            Toast.makeText(LoginActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        Toast.makeText(LoginActivity.this, "Error connecting with server", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof TextInputEditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent( event );
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }



}