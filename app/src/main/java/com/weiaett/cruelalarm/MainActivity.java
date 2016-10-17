package com.weiaett.cruelalarm;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {

    private ImageView matchView;
    private ImageComparator comparator;

    private static int[] images = new int[]{R.drawable.tv_small,
    R.drawable.back_small2, R.drawable.flower_small, R.drawable.other_small,
    R.drawable.oven_small, R.drawable.oven_small2};

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i("OpenCV", "Something went wrong with OpenCV loading");
                    return;
            }
            comparator = new ImageComparator();
            matchView = (ImageView) findViewById(R.id.imageView2);
            showMatch(images[0]);

            (findViewById(R.id.nextImageButton)).setOnClickListener(matchNextImage);
        }
    };

    View.OnClickListener matchNextImage = new View.OnClickListener() {

        private int imgCounter = 0;

        @Override
        public void onClick(View v) {
            imgCounter++;
            if(imgCounter < images.length) showMatch(images[imgCounter]);
        }
    };

    static{
        // System.loadLibrary("opencv_java");

       /* if(!OpenCVLoader.initDebug()) {
            Log.e("OpenCV","OpenCV initialization error");
        }
        else Log.i("OpenCV", "OpenCV initialized correctly");*/
    }

    protected void showMatch(int imgID){
        Bitmap match = comparator.matchImages(getApplicationContext(), R.drawable.back_small, imgID);
        matchView.setImageBitmap(match);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, loaderCallback);

//        Intent snippet: not working, because  storage size is 1MB.
//        Internal device memory required as a temporary bitmap storage

//        Intent i = new Intent(this, PreviewActivity.class);
//        i.putExtra(PreviewActivity.BITMAP, match);
//        startActivity(i);
    }
}
