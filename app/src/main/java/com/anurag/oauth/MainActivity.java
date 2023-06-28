package com.anurag.oauth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.io.ByteArrayOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    void loginRequired(){
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
    }
    SharedPreferences prefs;
    private static final int CAMERA_REQUEST = 1888;
    final MyApi myApi = APIClient.getClient().create(MyApi.class);

    ProfileResponse profile;
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

    public void afterFetchProfile(ProfileResponse profile){
        System.out.println(profile.getImgs());
        Intent it = new Intent(MainActivity.this, ImageDownloader.class);
        it.putStringArrayListExtra("imgs", profile.getImgs());
        startService(it);
        Toast.makeText(MainActivity.this, "welcome back", Toast.LENGTH_SHORT).show();
        User user = profile.getUser();
        greet.setText("Welcome "+user.getFirstName());
        MaterialButton galleryBtn = findViewById(R.id.exploreBtn);
        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent it = new Intent(MainActivity.this, GalleryActivity.class);
                it.putStringArrayListExtra("imgs", profile.getImgs());
                startActivity(it);
            }
        });
        MaterialButton captureBtn = findViewById(R.id.captureBtn);
        captureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
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
                    if(res!=null && res.isStatus()){
                        profile = res;
                        afterFetchProfile(res);
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

    private String encodeImage(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        String encImage = Base64.encodeToString(b, Base64.DEFAULT);
        return encImage;
    }

    public void uploadImage(String img){
        String accessToken = prefs.getString("accessToken", null);
        MyApi myApi = APIClient.getClient().create(MyApi.class);
        UploadRequest req = new UploadRequest();
        req.setImg(img);
        Call<UploadResponse> call = myApi.upload(req, accessToken);
        call.enqueue(new Callback<UploadResponse>() {
            @Override
            public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                UploadResponse res = response.body();
                if(res.isStatus()){
                    Toast.makeText(MainActivity.this, "Image Uploaded SuccessFully", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this, res.getMsg(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UploadResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Some Error Occoured", Toast.LENGTH_SHORT).show();
            }
        });
    }

    class UploadInBackground extends AsyncTask<String, Void, Void>{
        ProgressDialog progress;
        @Override
        protected Void doInBackground(String... strings) {
            String img = strings[0];
            fetchToken();
            String accessToken = prefs.getString("accessToken", null);
            MyApi myApi = APIClient.getClient().create(MyApi.class);
            UploadRequest req = new UploadRequest();
            req.setImg(img);
            Call<UploadResponse> call = myApi.upload(req, accessToken);
            call.enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    UploadResponse res = response.body();
                    if(res.isStatus()){
                        Toast.makeText(MainActivity.this, "Image Uploaded SuccessFully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this, res.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Some Error Occoured", Toast.LENGTH_SHORT).show();
                }
            });
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = ProgressDialog.show(MainActivity.this, "Uploading Image", "Please wait...");
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            progress.dismiss();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            String enc_img = encodeImage(photo);
            UploadInBackground upload = new UploadInBackground();
            upload.execute(enc_img);
        }
    }
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("super_secret", MODE_PRIVATE);
        greet = findViewById(R.id.greeting);
        fetchUserProfile();

    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}
