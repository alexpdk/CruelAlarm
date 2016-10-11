package com.weiaett.cruelalarm.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;

import com.weiaett.cruelalarm.R;
import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.Weekday;

import java.util.EnumSet;

/**
 * Created by Weiss_A on 26.09.2016.
 * Alarm Model
 */
public class Alarm {

    private EnumSet<Weekday> days = EnumSet.noneOf(Weekday.class);

    private int id;
    private boolean isActive = true;
    private Uri toneUri;
    private String tone = "По умолчанию";
    private boolean hasVibration = false;
    private String description = "";
    private String time;
    private Context context;

    public Alarm(Context context, String time) {
        SharedPreferences config = context.getSharedPreferences(context.
                getString(com.weiaett.cruelalarm.R.string.sp_config), Context.MODE_PRIVATE);
        this.context = context;
        this.time = time;
        this.toneUri = Uri.parse(config.getString(context.getString(com.weiaett.cruelalarm.R.string.sp_config_tone_uri),
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM).toString()));
        this.tone = RingtoneManager.getRingtone(context, this.toneUri).getTitle(context);
        this.hasVibration = config.getBoolean(context.getString(com.weiaett.cruelalarm.R.string.sp_config_vibration), false);
    }

    public Alarm(Context context) {
        this(context, context.getString(R.string.time_default));
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public Uri getToneUri() {
        return toneUri;
    }

    public void setToneUri(Uri toneUri) {
        this.toneUri = toneUri;
    }

    public boolean getHasVibration() {
        return hasVibration;
    }

    public void setHasVibration(boolean hasVibration) {
        this.hasVibration = hasVibration;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EnumSet<Weekday> getDays() {
        return days;
    }

    public void addDay(Weekday day){
        days.add(day);
    }

    public void removeDay(Weekday day) {
        days.remove(day);
    }

    public void deleteFromDatabase() {
        DBHelper.getInstance(context).deleteAlarm(this);
    }

    public void addToDatabase() {
        DBHelper.getInstance(context).addAlarm(this);
    }

    public void updateInDatabase() {
        DBHelper.getInstance(context).updateAlarm(this);
    }
}