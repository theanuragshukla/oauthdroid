package com.anurag.oauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.anurag.oauth.utils.TextValidator;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {
    Map<Integer, Boolean> errors = new HashMap<>();
    SharedPreferences prefs ;
    Map<String, String> values = new HashMap<>();
    int[] fields = {R.id.email, R.id.username, R.id.password, R.id.confirm_password, R.id.fname, R.id.lname};
    int[] layouts = {R.id.email_layout, R.id.username_layout, R.id.password_layout, R.id.confirm_password_layout, R.id.fname_layout, R.id.lname_layout};
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    int[] backIcons = {R.id.goTOEmail};
    int[] nextIcons = {R.id.goToPassword, R.id.goTOAbout};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);
        prefs = getSharedPreferences("super_secret", MODE_PRIVATE);
        ViewFlipper vf =  findViewById(R.id.flipper);
        for(int id: backIcons){
            MaterialButton btn =  findViewById(id);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vf.showPrevious();
                }
            });
        }
        for(int id : nextIcons){
            MaterialButton btn =  findViewById(id);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tag = btn.getTag().toString();
                    if(tag.equals("passwords")){
                        if(errors.getOrDefault(R.id.password, true) || errors.getOrDefault(R.id.confirm_password, true)){
                            Toast.makeText(SignupActivity.this, "Enter valid passwords to proceed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }else if (tag.equals("emailAndUsername")){
                        if(errors.getOrDefault(R.id.email, true) || errors.getOrDefault(R.id.username, true)){
                            Toast.makeText(SignupActivity.this, "Enter valid details to proceed", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    vf.showNext();
                }
            });
        }

        View.OnClickListener loginListener =  new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        };
        MaterialTextView goToLogin =  findViewById(R.id.backToLogin);
        goToLogin.setOnClickListener(loginListener);

        MaterialButton backToLogin = (MaterialButton) findViewById(R.id.goToLogin);
        backToLogin.setOnClickListener(loginListener);


        MaterialButton submitButton = (MaterialButton) findViewById(R.id.submitButton);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (errors.getOrDefault(R.id.email, true)) {
                    Toast.makeText(SignupActivity.this, "Enter a valid Email", Toast.LENGTH_SHORT).show();
                    return;
                } else if (errors.getOrDefault(R.id.username, true)) {
                    Toast.makeText(SignupActivity.this, "Enter a valid Username", Toast.LENGTH_SHORT).show();
                    return;
                } else if (errors.getOrDefault(R.id.password, true)) {
                    Toast.makeText(SignupActivity.this, "Enter a valid Password", Toast.LENGTH_SHORT).show();
                    return;
                } else if (errors.getOrDefault(R.id.fname, true)) {
                    Toast.makeText(SignupActivity.this, "Enter a valid Name", Toast.LENGTH_SHORT).show();
                    return;
                }
                SignupRequest request = new SignupRequest(values.get("fname"),values.get("lname"),values.get("email"),values.get("password"),values.get("username"));
                MyApi myApi = APIClient.getClient().create(MyApi.class);
                Call<SignupResponse> call = myApi.signup(request);
                call.enqueue(new Callback<SignupResponse>() {
                    @Override
                    public void onResponse(Call<SignupResponse> call, Response<SignupResponse> response) {
                        SignupResponse res = response.body();
                        if(res.isStatus()){
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("userId", res.getUser().getUid());
                            editor.putString("accessToken", res.getAccessToken());
                            editor.putString("refreshToken", res.getRefreshToken());
                            editor.apply();
                            Intent it = new Intent(SignupActivity.this, MainActivity.class);
                            it.putExtra("data", res.getUser());
                            startActivity(it);
                        }else{
                            Toast.makeText(SignupActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<SignupResponse> call, Throwable t) {
                        Toast.makeText(SignupActivity.this, "Error connecting with server", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, LoginActivity.class));
                    }
                });

            }
        });



        for(int i = 0; i< fields.length; i++){
            EditText editText = findViewById(fields[i]);
            TextInputLayout layout = findViewById(layouts[i]);
            int finalI = i;
            editText.addTextChangedListener(new TextValidator(editText) {
                @Override
                public void validate(TextView textView, String text) {
                    String tag = editText.getTag().toString();
                    String errorMsg = null;
                    values.put(tag, text);
                    if(tag.equals("email")){
                        boolean valid = isValidEmail(text);
                        errorMsg = valid ? null : "Enter a valid email address";
                    }else if(tag.equals("username")){
                        if(text.length()<3){
                            errorMsg = "Username too short";
                        }else{
                            boolean valid = text.matches("^[a-zA-Z0-9._]{3,}$");
                            errorMsg = valid ? null : "Username should only contain:\nAlphanumeric character\ndot(.) and underscore(_)";
                        }
                    }
                    else if(tag.equals("password")){
                        if(text.length()<8){
                            errorMsg = "Password too short";
                        }else{
                            errorMsg = null;
                        }

                    }
                    else if(tag.equals("confirm_password")){
                        EditText pass = findViewById(R.id.password);
                        if(pass.getText().toString().equals(text)){
                            errorMsg=null;
                        }else{
                            errorMsg="Passwords do not match";
                        }
                    }
                    else if(tag.equals("fname")){
                        errorMsg = text.length()<3  ? "Enter a valid Name" : null;
                    }
                    else if(tag.equals("lname")){
                    }
                    errors.put(fields[finalI], errorMsg!=null);
                    layout.setError(errorMsg);
                }
            });
        }

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if ( v instanceof EditText) {
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
}