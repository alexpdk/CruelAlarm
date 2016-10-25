package com.weiaett.cruelalarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;

public class MainActivity extends AppCompatActivity {

    public static final String FirstImage = "fImg";
    public static final String SecondImage = "sImg";

    private static final String MAIN = "MatchMain";

    private ImageView matchView;
    private TextView statusView;

    // compared image
    private static int sample = R.drawable.back_small2;
    // images to compare sample with
    private static int[] images = new int[]{
        R.drawable.m1, R.drawable.m2, R.drawable.back_small, R.drawable.back_small2, R.drawable.b_oven,
        R.drawable.oven_small, R.drawable.tv_small, R.drawable.flower_small,
        R.drawable.other_small, R.drawable.oven_small2};

    private ComparisonReceiver compReceiver;

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i("OpenCV", "Something went wrong with OpenCV load");
                    return;
            }
            matchView = (ImageView) findViewById(R.id.imageView2);
            statusView = (TextView) findViewById(R.id.statusView);
            showMatch(images[0]);

            (findViewById(R.id.nextImageButton)).setOnClickListener(matchNextImage);

            IntentFilter filter = new IntentFilter(ComparatorService.Companion.getBROADCAST_ACTION());
            compReceiver = new ComparisonReceiver(matchView, statusView);
            LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(compReceiver, filter);
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


    protected void showMatch(int imgID){
        Log.d("CruelAlarmMain", "showMatch");
        Intent i = new Intent(this, ComparatorService.class);
        i.putExtra(FirstImage, sample);
        i.putExtra(SecondImage, imgID);
        startService(i);

       // Comparator comparator = new Comparator(getApplicationContext(), sample, imgID);
       // Bitmap match = comparator.matchImages();
       // matchView.setImageBitmap(match);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, loaderCallback);
    }

    @Override
    protected void onStop()
    {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(compReceiver);
        super.onStop();
    }
}
