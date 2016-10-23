package com.weiaett.cruelalarm.alarm_list;

import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.SettingsActivity;
import com.weiaett.cruelalarm.graphics.SmoothLLManager;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.utils.Utils;

import java.util.Calendar;
import java.util.List;


public class AlarmListActivity extends AppCompatActivity
        implements RecyclerView.OnItemTouchListener {

    static final int TONE_PICKER_ROOT_ADAPTER = 0;
    static final int TONE_PICKER_ROOT_SETTINGS = 1;

    private AlarmListAdapter alarmListAdapter;
    private GestureDetectorCompat gestureDetector;
    private RecyclerViewOnGestureListener recyclerViewOnGestureListener;
    private ActionMode actionMode;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        recyclerView = (RecyclerView) this.findViewById(R.id.list);
        recyclerView.setLayoutManager(new SmoothLLManager(this, LinearLayoutManager.VERTICAL, false));

        alarmListAdapter = new AlarmListAdapter(DBHelper.getInstance(this).getAllAlarms(this), recyclerView);
        recyclerView.setAdapter(alarmListAdapter);

        recyclerView.addOnItemTouchListener(this);
        recyclerViewOnGestureListener = new RecyclerViewOnGestureListener();
        gestureDetector = new GestureDetectorCompat(this, recyclerViewOnGestureListener);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(getString(R.string.app_name));

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog timePickerDialog = new TimePickerDialog(AlarmListActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                                calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                                calendar.set(Calendar.MINUTE, selectedMinute);
                                alarmListAdapter.addAlarm(new Alarm(AlarmListActivity.this,
                                        Utils.getFormattedTime(AlarmListActivity.this, calendar)));
                            }
                        }, hour, minute, true);
                timePickerDialog.show();
            }
        });

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener(){
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy){
                if (actionMode != null && fab.isShown()) {
                    fab.hide();
                    Log.d("fab", "hide, action");
                }
                if (dy > 0 && fab.isShown()) {
                    fab.hide();
                    Log.d("fab", "hide");
                }
                if (dy <= 0 && !fab.isShown() && actionMode == null) {
                    fab.show();
                    Log.d("fab", "show");
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(AlarmListActivity.this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent ringtoneIntent) {
       switch (requestCode) {
            case TONE_PICKER_ROOT_ADAPTER:
                alarmListAdapter.onActivityResult(resultCode, ringtoneIntent);
                break;
            case TONE_PICKER_ROOT_SETTINGS:
                recyclerViewOnGestureListener.onActivityResult(resultCode, ringtoneIntent);
                break;
        }
    }

    boolean isInActionMode() {
        return actionMode != null;
    }

    private void toggleSelection(int position) {
        alarmListAdapter.toggleSelection(position);
        int count = alarmListAdapter.getSelectedItemCount();
        if (count > 0) {
            actionMode.setTitle("Выбрано: " + alarmListAdapter.getSelectedItemCount());
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

    private class RecyclerViewOnGestureListener extends GestureDetector.SimpleOnGestureListener {

        private Uri commonRingtoneUri = null;
        private String commonRingtoneTitle = null;

        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_bulk_alarm_settings, null);
        final TextView toneView = (TextView) dialogView.findViewById(R.id.tvTone);
        final TextView toneViewLabel = (TextView) dialogView.findViewById(R.id.tvToneLabel);
        final Switch vibrationView = (Switch) dialogView.findViewById(R.id.switchVibration);
        final TextView photoView = (TextView) dialogView.findViewById(R.id.tvPhotoManager);
        final AlertDialog dialog;
        List<Integer> selectedItemPositions;

        RecyclerViewOnGestureListener() {
            photoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO: call photo picker dialog
                }
            });

            toneView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.callTonePicker(AlarmListActivity.this,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), 1);
                }
            });
            toneViewLabel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utils.callTonePicker(AlarmListActivity.this,
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), 1);
                }
            });

            final AlertDialog.Builder builder = new AlertDialog.Builder(AlarmListActivity.this);
            builder.setView(dialogView)
                    .setTitle("Настройки")
                    .setNegativeButton("Отмена", null)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                                alarmListAdapter.setAlarmParams(selectedItemPositions.get(i),
                                        commonRingtoneUri, commonRingtoneTitle, vibrationView.isChecked());
                            }
                            actionMode.finish();
                        }
                    });
            dialog = builder.create();
        }

        void onActivityResult(int resultCode, Intent ringtoneIntent) {
            if (resultCode == RESULT_OK) {
                commonRingtoneUri = ringtoneIntent.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                commonRingtoneTitle = RingtoneManager.getRingtone(AlarmListActivity.this,
                        commonRingtoneUri).getTitle(AlarmListActivity.this);
                toneView.setText(commonRingtoneTitle);
            }
        }

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
                            alarmListAdapter.performClickOnAlarmCard(recyclerView.getChildAdapterPosition(view));
                        }
                    }
                });
            }
            return super.onDown(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if (alarmListAdapter.getAlarmsCount() == 0 || actionMode != null) {
                return;
            }
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            actionMode = startSupportActionMode(new ActionMode.Callback(){
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (fab.isShown()) {
                        fab.hide();
                        Log.d("fab", "hide, action init");
                    }
                    MenuInflater inflater = actionMode.getMenuInflater();
                    inflater.inflate(R.menu.action_mode_menu, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode actionMode) {
                    AlarmListActivity.this.actionMode = null;
                    alarmListAdapter.clearSelections();
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (!fab.isShown()) {
                        fab.show();
                        Log.d("fab", "show, action done");
                    }
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    selectedItemPositions = alarmListAdapter.getSelectedItems();
                    switch (menuItem.getItemId()) {
                        case R.id.action_delete:
                            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                                alarmListAdapter.deleteAlarm(selectedItemPositions.get(i));
                            }
                            actionMode.finish();
                            return true;
                        case R.id.action_select:
                            if (selectedItemPositions.size() < alarmListAdapter.getAlarmsCount()) {
                                alarmListAdapter.selectAll();
                                actionMode.setTitle("Выбрано: " + alarmListAdapter.getSelectedItemCount());
                            } else {
                                alarmListAdapter.clearSelections();
                                actionMode.finish();
                            }
                            return true;
                        case R.id.action_settings:
                            dialog.show();
                        default:
                            return false;
                    }
                }
            });
            toggleSelection(recyclerView.getChildAdapterPosition(view));
            alarmListAdapter.closeExpanded();
            alarmListAdapter.notifyDataSetChanged();
            super.onLongPress(e);
        }
    }
}
