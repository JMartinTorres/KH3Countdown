package com.example.javier.kh3countdown;

import android.os.AsyncTask;

public class HideButtons extends AsyncTask<Void, Void, Void> {

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (!isCancelled()) {
            Countdown.hideMute();
            Countdown.hideSongs();
        }
    }
}
