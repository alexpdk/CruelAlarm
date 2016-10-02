package com.weiaett.cruelalarm;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TimePicker;

import com.weiaett.cruelalarm.graphics.SmoothLLManager;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.utils.Utils;

import java.util.Calendar;
import java.util.List;


public class AlarmListActivity extends AppCompatActivity
        implements RecyclerView.OnItemTouchListener {

    private AlarmListAdapter alarmListAdapter;
    private GestureDetectorCompat gestureDetector;
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
        gestureDetector =
                new GestureDetectorCompat(this, new RecyclerViewOnGestureListener());


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setActionBarTitle(getString(R.string.app_name));

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
                                        Utils.getFormattedTime(calendar)));
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
                }
                if (dy > 0 && fab.isShown())
                    fab.hide();
                if (dy <= 0 && !fab.isShown())
                    fab.show();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent ringtoneIntent) {
        alarmListAdapter.onActivityResult(requestCode, resultCode, ringtoneIntent);
    }

    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
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
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
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
            return super.onSingleTapConfirmed(e);
        }

        public void onLongPress(MotionEvent e) {
            View view = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if (alarmListAdapter.getChildrenCount() == 0 || actionMode != null) {
                return;
            }
            actionMode = startSupportActionMode(new ActionMode.Callback(){
                @Override
                public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                    FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
                    if (fab.isShown())
                        fab.hide();
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
                    if (!fab.isShown())
                        fab.show();
                }

                @Override
                public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                    List<Integer> selectedItemPositions = alarmListAdapter.getSelectedItems();
                    switch (menuItem.getItemId()) {
                        case R.id.action_delete:
                            for (int i = selectedItemPositions.size() - 1; i >= 0; i--) {
                                alarmListAdapter.deleteAlarm(selectedItemPositions.get(i));
                            }
                            actionMode.finish();
                            return true;
                        case R.id.action_select:
                            if (selectedItemPositions.size() < alarmListAdapter.getChildrenCount()) {
                                alarmListAdapter.selectAll();
                                actionMode.setTitle("Выбрано: " + alarmListAdapter.getSelectedItemCount());
                            } else {
                                alarmListAdapter.clearSelections();
                                actionMode.finish();
                            }
                            return true;
                        default:
                            return false;
                    }
                }
            });
            toggleSelection(recyclerView.getChildAdapterPosition(view));
            alarmListAdapter.closeExpanded();
            alarmListAdapter.notifyDataSetChanged();
            // TODO: change bg & hide buttons for all children
            super.onLongPress(e);
        }
    }
}
