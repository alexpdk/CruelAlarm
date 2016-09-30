package com.weiaett.cruelalarm;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.TimePicker;

import com.weiaett.cruelalarm.graphics.SmoothLLManager;
import com.weiaett.cruelalarm.models.Alarm;
import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.utils.Utils;

import java.util.Calendar;


public class AlarmListActivity extends AppCompatActivity {

    private AlarmListAdapter alarmListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_list);

        RecyclerView recyclerView = (RecyclerView) this.findViewById(R.id.list);
        recyclerView.setLayoutManager(new SmoothLLManager(this, LinearLayoutManager.VERTICAL, false));

        alarmListAdapter = new AlarmListAdapter(DBHelper.getInstance(this).getAllAlarms(this), recyclerView);
        recyclerView.setAdapter(alarmListAdapter);

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
                if (dy > 0 && fab.isShown())
                    fab.hide();
                if (dy <= 0 && !fab.isShown())
                    fab.show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent ringtoneIntent) {
        alarmListAdapter.onActivityResult(requestCode, resultCode, ringtoneIntent);
    }
}
