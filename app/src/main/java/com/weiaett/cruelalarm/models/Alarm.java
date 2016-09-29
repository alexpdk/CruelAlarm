package com.weiaett.cruelalarm.models;

import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import com.weiaett.cruelalarm.utils.DBHelper;
import com.weiaett.cruelalarm.WeekDay;

import java.util.EnumSet;

/**
 * Created by Weiss_A on 26.09.2016.
 * Alarm Model
 */
public class Alarm {

    private EnumSet<WeekDay> days = EnumSet.noneOf(WeekDay.class);

    private int id;
    private boolean isActive = true;
    private Uri toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    private String tone = "По умолчанию";
    private boolean hasVibration = false;
    private String description = "";
    private String time;
    private Context context;

    public Alarm(Context context, String time) {
        this.time = time;
        this.context = context;
    }

    public Alarm(Context context) {
        this.context = context;
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

    public EnumSet<WeekDay> getDays() {
        return days;
    }

    public void addDay(WeekDay day){
        days.add(day);
    }

    public void removeDay(WeekDay day) {
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