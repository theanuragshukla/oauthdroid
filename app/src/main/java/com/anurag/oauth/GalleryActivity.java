package com.anurag.oauth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class GalleryActivity extends AppCompatActivity {
    Display display;
    int width;
    int sz;
    ArrayList<String> imgs;
    SharedPreferences prefs;
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private ArrayList<String> mImageResources;

        private Bitmap loadImage(String path, String uid)
        {

            try {
                File f=new File(path, uid+".jpg");
                Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));
                return b;
            }
            catch (FileNotFoundException e)
            {
                e.printStackTrace();
            }
return null;
        }
        public ImageAdapter(Context context, ArrayList<String> imageResources) {
            mContext = context;
            mImageResources = imageResources;
        }

        @Override
        public int getCount() {
            return mImageResources.size();
        }

        @Override
        public Object getItem(int position) {
            return mImageResources.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                GridView.LayoutParams parms = new GridView.LayoutParams(sz,sz);
                imageView.setLayoutParams(parms);
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                imageView = (ImageView) convertView;
            }
            String uid = String.valueOf(mImageResources.get(position));
            String path = prefs.getString(uid, "/");
            imageView.setImageBitmap(loadImage(path, uid));
            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(GalleryActivity.this, String.valueOf(position), Toast.LENGTH_SHORT).show();
                }
            });
            return imageView;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        getSupportActionBar().setTitle("Gallery");
        prefs = getSharedPreferences("super_secret", MODE_PRIVATE);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        display= getWindowManager().getDefaultDisplay();
        width = display.getWidth();
        sz =(int) Math.floor(width/3);
        if(getIntent().hasExtra("imgs")){
            imgs = getIntent().getStringArrayListExtra("imgs");
            GridView gridView = findViewById(R.id.imageGrid);
            ImageAdapter adapter = new ImageAdapter(this, imgs);
            gridView.setAdapter(adapter);
        }else{
            Intent it =new Intent(GalleryActivity.this, MainActivity.class);
            it.putStringArrayListExtra("imgs", imgs);
            startActivity(it);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return true;
    }
}
