package com.anurag.oauth;

import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ImageDownloader extends Service {
    SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        prefs = getSharedPreferences("super_secret", Context.MODE_PRIVATE);
    }

    class FetchImage extends AsyncTask<String, Void, Void>{

        protected String saveBitmap(Bitmap bitmapImage, String uid){
            ContextWrapper cw = new ContextWrapper(getApplicationContext());
            File directory = cw.getDir("tempImages", Context.MODE_PRIVATE);
            File mypath=new File(directory,uid+".jpg");
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(mypath);
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return directory.getAbsolutePath();
        }

        @Override
        protected Void doInBackground(String... strings) {
            String uid = strings[0];
            String accessToken = prefs.getString("accessToken", null);
            System.out.println("fetching "+uid+" "+ accessToken);
            MyApi myApi = APIClient.getClient().create(MyApi.class);
            Call<FetchImageResponse> call = myApi.fetchImage(accessToken , uid);
            call.enqueue(new Callback<FetchImageResponse>() {
                @Override
                public void onResponse(Call<FetchImageResponse> call, Response<FetchImageResponse> response) {
                    FetchImageResponse res = response.body();
                    if(res.isStatus()){
                        String img = res.getImg();
                        byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
                        Bitmap bmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        String path = saveBitmap(bmp, uid);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString(uid, path);
                        editor.commit();
                        editor.apply();
                    }else{
                        Toast.makeText(ImageDownloader.this, res.getMsg(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<FetchImageResponse> call, Throwable t) {
                }
            });
            return null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ArrayList<String> arr = intent.getStringArrayListExtra("imgs");
        for(String uid: arr){
            String path = prefs.getString(uid, null);
            if(path!=null){
                File f = new File(path);
                if(f.exists()){
                   continue;
                }
                else{
                    FetchImage fetch = new FetchImage();
                    fetch.execute(uid);
                }
            }else{
                FetchImage fetch = new FetchImage();
                fetch.execute(uid);
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}