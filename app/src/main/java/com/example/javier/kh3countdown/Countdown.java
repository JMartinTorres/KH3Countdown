package com.example.javier.kh3countdown;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
    private static FloatingActionButton btnMute, btnSongs;

    private static final DateTime RELEASE_DATE = new DateTime(2019, 1, 29, 0, 0);
    private long days;
    private long hours;
    private long minutes;
    private long seconds;

    private MediaPlayer mediaPlayer;
    private boolean muted = false;
    private static final String songsPath = "android.resource://com.example.javier.kh3countdown/raw/";
    private ArrayList<String> songs = new ArrayList<>();
    private Random songSelector;

    private HideButtons hbTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_countdown);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        layout = findViewById(R.id.layout);
        txCountdown = findViewById(R.id.txCountdown);
        btnMute = findViewById(R.id.btnMute);
        btnSongs = findViewById(R.id.btnSongs);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btnMute.getVisibility() != View.VISIBLE) {
                    btnMute.setVisibility(View.VISIBLE);
                    btnSongs.setVisibility(View.VISIBLE);
                    hbTask = (HideButtons) new HideButtons().execute();
                }
            }
        });

        btnMute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                muteUnmute();
            }
        });

        btnSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displaySongs();
            }
        });

        try {
            Interval interval = new Interval(new Instant(), RELEASE_DATE);
            initCountDown(interval);
        } catch (IllegalArgumentException e) { // Probably the game is already out.
            e.printStackTrace();
            txCountdown.setText("The magic is back.");
        }

        getSongs();
        hbTask = (HideButtons) new HideButtons().execute();

    }

    private void initCountDown(Interval interval) {
        new CountDownTimer(interval.toDurationMillis(), 1000) {

            public void onTick(long millisUntilFinished) {
                setTime(millisUntilFinished);
            }

            public void onFinish() {
                txCountdown.setText("The magic is back.");
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        songSelector = new Random(System.currentTimeMillis());
        mediaPlayerSettings();
        if (muted)
            mediaPlayer.setVolume(0, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    private void setTime(long millisUntilFinished) {

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


    private void mediaPlayerSettings() {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri(-1));
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(getApplicationContext(), songUri(-1));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void mediaPlayerSettings(final int which) {
        mediaPlayer = MediaPlayer.create(getApplicationContext(), songUri(which));
        mediaPlayer.start();
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                try {
                    mediaPlayer.reset();
                    mediaPlayer.setDataSource(getApplicationContext(), songUri(-1));
                    mediaPlayer.prepare();
                    mediaPlayer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void changeSong(int which) {
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayerSettings(which);
    }

    private Uri songUri(int pos) {
        if (pos == -1)
            return Uri.parse(songsPath + songs.get(songSelector.nextInt(songs.size())));
        else
            return Uri.parse(songsPath + songs.get(pos));
    }

    private void muteUnmute() {

        if (!muted) {
            mediaPlayer.setVolume(0, 0);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            btnMute.setImageResource(R.drawable.speaker);
        } else {
            if (mediaPlayer.isPlaying())
                mediaPlayer.setVolume(1, 1);
            else
                mediaPlayerSettings();
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            btnMute.setImageResource(R.drawable.mute);
        }
        muted = !muted;

    }

    public static void hideMute() {
        btnMute.setVisibility(View.GONE);
    }

    public static void hideSongs() {
        btnSongs.setVisibility(View.GONE);
    }

    private void displaySongs() {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(Countdown.this);
        builderSingle.setTitle("Choose another song to play");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(Countdown.this,
                android.R.layout.select_dialog_singlechoice, songs);

        builderSingle.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                changeSong(which);
                dialog.dismiss();
            }
        });
        builderSingle.show();
    }

    public void getSongs() {
        Field[] fields = R.raw.class.getFields();
        for (int count = 0; count < fields.length; count++) {
            songs.add(fields[count].getName());
        }
    }

}
