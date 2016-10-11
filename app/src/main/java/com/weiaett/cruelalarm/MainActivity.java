package com.weiaett.cruelalarm;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    static{
        // TODO test whether it works without explicit loadLibrary
        System.loadLibrary("opencv_java");
        if(!OpenCVLoader.initDebug()) {
            Log.e("OpenCV","OpenCV initialization error");
        }
        else Log.i("OpenCV", "OpenCV initialized correctly");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageComparator comparator = new ImageComparator();
        Bitmap match = comparator.matchImages(getApplicationContext(), R.drawable.s1, R.drawable.s3);

        ImageView img = (ImageView) findViewById(R.id.imageView2);
        img.setImageBitmap(match);

//        Intent snippet: not working, because  storage size is 1MB.
//        Internal device memory required as a temporary bitmap storage

//        Intent i = new Intent(this, PreviewActivity.class);
//        i.putExtra(PreviewActivity.BITMAP, match);
//        startActivity(i);
    }
}
