package com.weiaett.cruelalarm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

public class PreviewActivity extends AppCompatActivity {

    public static String BITMAP = "Bitmap Extra";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        Intent intent =  getIntent();
        Bitmap bmp = intent.getParcelableExtra(BITMAP);
        ImageView img = (ImageView) findViewById(R.id.imageView);
        img.setImageBitmap(bmp);
    }
}
