package com.weiaett.cruelalarm;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.weiaett.cruelalarm.utils.Utils;

public class WakeUpActivity extends AppCompatActivity {

    MediaPlayer player;
    int interval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Utils.lockOn(this);
        final Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        setContentView(R.layout.activity_wake_up);

        // lock virtual home and back buttons
        this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD_DIALOG);

        Uri toneUri;
        String description;

        try {
            toneUri =  Uri.parse(getIntent().getStringExtra("tone"));
            description = getIntent().getStringExtra("my description");
        }
        catch (RuntimeException e) {
            toneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            description = "Будильник";
        }

        ((TextView) this.findViewById(R.id.tvDescription)).setText(description);

        player = MediaPlayer.create(this, toneUri);
        player.setLooping(true);
        player.start();

        SharedPreferences config = this.getSharedPreferences(this.getString(R.string.sp_config),
                Context.MODE_PRIVATE);
        interval = config.getInt(this.getString(R.string.sp_config_interval), 5);

        findViewById(R.id.btnHold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holdOver();
            }
        });
        findViewById(R.id.btnTurnOff).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                terminate();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_VOLUME_UP:
                holdOver();
                return true;
            // it works on some old devices
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_MENU:
            case KeyEvent.KEYCODE_HOME:
                return false;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

//    @Override
//    protected void onUserLeaveHint() {
//        super.onUserLeaveHint();
//        ActivityManager activityManager = (ActivityManager) getApplicationContext()
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        activityManager.moveTaskToFront(getTaskId(), 0);
//    }

    @Override
    protected void onStop() {
        Log.d("WakeUp", "on stop");
        holdOver();
        Utils.lockOff();
        super.onStop();
    }

    private void holdOver() {
        Toast.makeText(this, String.format("Будильник отложен на %s минут", interval),
                Toast.LENGTH_SHORT).show();
        terminate();
    }

    private void terminate() {
        player.stop();
        if (Build.VERSION.SDK_INT > 21)
            finishAndRemoveTask();
        else {
            int iWantToExit = 10 / 0;
        }
    }
}

// TODO: call wake up, timetable wake up

