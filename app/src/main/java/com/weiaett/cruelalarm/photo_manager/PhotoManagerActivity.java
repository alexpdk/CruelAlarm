package com.weiaett.cruelalarm.photo_manager;

import android.app.Activity;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.img_proc.ComparatorService;
import com.weiaett.cruelalarm.img_proc.ComparisonActivity;
import com.weiaett.cruelalarm.img_proc.ImgProcessor;
import com.weiaett.cruelalarm.utils.ImageLoader;

import java.net.URI;
import java.io.File;
import java.util.List;

public class PhotoManagerActivity extends AppCompatActivity implements
        PhotoManagerFragment.OnFragmentInteractionListener,
        RecyclerView.OnItemTouchListener {

    private static final int CAMERA_REQUEST = 0;
    private Uri fileUri;
    private ActionMode actionMode;
    private GestureDetectorCompat gestureDetector;
    private PhotoManagerActivity.RecyclerViewOnGestureListener recyclerViewOnGestureListener;
    private PhotoManagerFragment photoManagerFragment;
    private PhotoManagerAdapter photoManagerAdapter;
    private RecyclerView recyclerView;
    private String SCAN_PATH;
    private static final String FILE_TYPE = "image/*";


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
                fileUri = ImageLoader.prepareUri(PhotoManagerActivity.this);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            }
        });

        recyclerViewOnGestureListener = new PhotoManagerActivity.RecyclerViewOnGestureListener();
        gestureDetector = new GestureDetectorCompat(this, recyclerViewOnGestureListener);

        photoManagerFragment = (PhotoManagerFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragmentPhotoManager);

        photoManagerAdapter = photoManagerFragment.getPhotoManagerAdapter();
        recyclerView = photoManagerFragment.getRecyclerView();
        recyclerView.addOnItemTouchListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (actionMode != null && fab.isShown()) {
                    fab.hide();
                }
                if (dy > 0 && fab.isShown()) {
                    fab.hide();
                }
                if (dy <= 0 && !fab.isShown() && actionMode == null) {
                    fab.show();
                }
            }
        });
    }

    private void toggleSelection(int position) {
        photoManagerAdapter.toggleSelection(position);
        int count = photoManagerAdapter.getSelectedItemCount();
        if (count > 0) {
            actionMode.setTitle(getString(R.string.selection_count) + photoManagerAdapter.getSelectedItemCount());
        } else {
            actionMode.finish();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {
    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean b) {
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            //photoManagerFragment.addPhoto(new File(fileUri.getPath()));

            Uri newUri = ImgProcessor.Companion.process(getApplicationContext(), fileUri);
            if(newUri != null){
                photoManagerFragment.addPhoto(new File(newUri.getPath()));
                Toast.makeText(getApplicationContext(), "Снимок успешно добавлен",
                        Toast.LENGTH_LONG).show();
            }else{
                Toast.makeText(getApplicationContext(), "На снимке не хватает\n контрастных участков",
                        Toast.LENGTH_LONG).show();
            }
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

    private  MediaScannerConnection conn;
    private void notifySystemWithImage(final File imageFile) {

        conn = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {

            @Override
            public void onScanCompleted(String path, Uri uri) {

                try {
                    if (uri != null) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "image/*");
                        startActivity(intent);
                    }
                } finally {
                    conn.disconnect();
                    conn = null;
                }
            }

            @Override
            public void onMediaScannerConnected() {
                conn.scanFile(imageFile.getAbsolutePath(), "*/*");

            }
        });

        conn.connect();
    }

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        List<Integer> selectedItemPositions;

        @Override
        public boolean onDown(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (view != null) {
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (actionMode != null) {
                            toggleSelection(recyclerView.getChildAdapterPosition(view));
                        } else {
                            //notifySystemWithImage(new File("/storage/emulated/0/Weiaett/alarm/1479816124572.png"));
                        }
                    }
                });
            }
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (photoManagerAdapter.getPhotosCount() == 0 || actionMode != null) {
                return;
            }
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            actionMode = startSupportActionMode(new ActionMode.Callback(){
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (fab.isShown()) {
                        fab.hide();
                    }
                    MenuInflater inflater = actionMode.getMenuInflater();
                    inflater.inflate(R.menu.action_mode_photo_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    PhotoManagerActivity.this.actionMode = null;
                    photoManagerAdapter.clearSelections();
                    photoManagerAdapter.setCheckboxVisible(false);
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (!fab.isShown()) {
                        fab.show();
                    }
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    selectedItemPositions = photoManagerAdapter.getSelectedItems();
                    switch (menuItem.getItemId()) {
                        case R.id.action_delete:
                            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                                photoManagerAdapter.deleteItem(selectedItemPositions.get(i));
                            }
                            actionMode.finish();
                            return true;
                        case R.id.action_select:
                            if (selectedItemPositions.size() < photoManagerAdapter.getPhotosCount()) {
                                photoManagerAdapter.selectAll();
                                actionMode.setTitle(getString(R.string.selection_count) +
                                        photoManagerAdapter.getSelectedItemCount());
                            } else {
                                photoManagerAdapter.clearSelections();
                                actionMode.finish();
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });
            toggleSelection(recyclerView.getChildAdapterPosition(view));
            photoManagerAdapter.setCheckboxVisible(true);
            photoManagerAdapter.notifyDataSetChanged();
            super.onLongPress(e);
        }
    }
}
