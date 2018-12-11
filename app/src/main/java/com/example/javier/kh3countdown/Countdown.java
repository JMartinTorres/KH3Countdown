package com.example.javier.kh3countdown;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Interval;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Random;

public class Countdown extends AppCompatActivity {

    private RelativeLayout layout;
    private TextView txCountdown;
    private static Button btnMute;

    private static final DateTime RELEASE_DATE = new DateTime(2019,1,29,0,0);
    private long days;
    private long hours;
    private long minutes;
    private long seconds;

    private MediaPlayer mediaPlayer;
    private static final String songsPath = "android.resource://com.example.javier.kh3countdown/raw/";
    private ArrayList<String> songs = new ArrayList<>();
    private Random songSelector = new Random();

    private HideButton hbTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_countdown);

        layout = findViewById(R.id.layout);
        txCountdown = findViewById(R.id.txCountdown);
        btnMute = findViewById(R.id.btnMute);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnMute.getVisibility() != View.VISIBLE) {
                    btnMute.setVisibility(View.VISIBLE);
                    hbTask = (HideButton) new HideButton().execute();
                }
            }
        });

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hbTask = (HideButton) new HideButton().execute();
            }
        });

        // Comparar por si ya ha salido ;)
        Interval interval = new Interval(new Instant(),RELEASE_DATE);

        new CountDownTimer(interval.toDurationMillis(), 1000) {

            public void onTick(long millisUntilFinished) {
                setTime(millisUntilFinished);
            }

            public void onFinish() {
                txCountdown.setText("The magic is back.");
            }
        }.start();

        getSongs();
        hbTask = (HideButton) new HideButton().execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mediaPlayerSettings();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void mediaPlayerSettings() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), changeSong());
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(getApplicationContext(),changeSong());
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private Uri changeSong() {
        return Uri.parse(songsPath + songs.get(songSelector.nextInt(songs.size())));
    }

    private void setTime (long millisUntilFinished){

        long leftMills;

        days = millisUntilFinished / 86400000;
        leftMills = millisUntilFinished % 86400000;

        hours = leftMills / 3600000;
        leftMills %= 3600000;

        minutes = leftMills / 60000;
        leftMills %= 60000;

        seconds = leftMills / 1000;

        txCountdown.setText((days > 0 ? days + "d " : "") +
                            (hours > 0 ? hours + "h " : "") +
                            (minutes > 0 ? minutes + "m " : "") +
                            (seconds > 0 ? seconds + "s " : ""));
    }

    public static void hideMute() {
        btnMute.setVisibility(View.GONE);
    }

    public void getSongs(){
        Field[] fields=R.raw.class.getFields();
        for(int count=0; count < fields.length; count++){
            songs.add(fields[count].getName());
        }
    }
}
