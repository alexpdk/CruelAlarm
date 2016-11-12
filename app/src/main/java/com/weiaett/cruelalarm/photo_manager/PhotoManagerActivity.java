package com.weiaett.cruelalarm.photo_manager;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.img_proc.ComparisonActivity;
import com.weiaett.cruelalarm.img_proc.ImgProcessor;
import com.weiaett.cruelalarm.utils.ImageLoader;

import java.net.URI;
import java.util.List;

public class PhotoManagerActivity extends AppCompatActivity implements PhotoManagerFragment.OnFragmentInteractionListener{

    private static final int CAMERA_REQUEST = 0;

    private Uri fileUri, prevUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_manager);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                fileUri = ImageLoader.prepareFile(PhotoManagerActivity.this);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //Bitmap photo = (Bitmap) data.getExtras().get("data");

            Uri newUri = ImgProcessor.Companion.process(getApplicationContext(), fileUri);

            if(prevUri != null && newUri != null) {
                Intent i = new Intent(this, ComparisonActivity.class);
                i.putExtra(ComparisonActivity.Companion.getPATH_1(), newUri.getPath());
                i.putExtra(ComparisonActivity.Companion.getPATH_2(), prevUri.getPath());
                startActivity(i);
            }
            prevUri = newUri;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(List<String> photos, int alarmId) {

    }
}
