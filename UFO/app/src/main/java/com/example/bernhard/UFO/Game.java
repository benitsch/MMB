package com.example.bernhard.UFO;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;


public class Game extends Activity {

    private MediaPlayer mp;
    private int mpCurrPos;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //turn title off
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        //set to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mp = MediaPlayer.create(this, R.raw.background);

        mp.setLooping(true);
        mp.setVolume(.5f,.5f);
        mp.start();

        setContentView(new GamePanel(this));
    }

    @Override
    protected void onPause() {
        super.onPause();
        mpCurrPos = mp.getCurrentPosition();
        mp.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mp.seekTo(mpCurrPos);
        mp.start();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mp.stop();
        mp.release();
        finish();
    }


}
