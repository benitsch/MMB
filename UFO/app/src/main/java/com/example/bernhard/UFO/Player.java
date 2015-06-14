package com.example.bernhard.UFO;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;

/**
 * Created by Bernhard on 24.05.2015.
 */
public class Player extends GameObject {
    private Bitmap spritesheet, powerUpSheet;
    private int score;

    private boolean up;
    private boolean playing;
    private Animation animation = new Animation();
    private long startTime;

    private boolean powerUpOn = false;
    long PowerUpTime = 0;

    public Player(Bitmap res, int w, int h, int numFrames) {

        x = 100;
        y = GamePanel.HEIGHT / 2;
        dy = 0;
        score = 0;
        height = h;
        width = w;

        spritesheet = res;

        setAnimation(res, numFrames);

        startTime = System.nanoTime();

    }

    private void setAnimation(Bitmap res, int numFrames) {
        Bitmap[] image = new Bitmap[numFrames];
        Bitmap spritesheet = res;

        for (int i = 0; i < image.length; i++) {
            image[i] = Bitmap.createBitmap(spritesheet, i * width, 0, width, height);
        }

        animation.setFrames(image);
        animation.setDelay(10);
    }

    public void setUp(boolean b) {
        up = b;
    }

    public void update() {
        long elapsed = (System.nanoTime() - startTime) / 1000000;
        if (elapsed > 100) {
            score++;
            startTime = System.nanoTime();
        }
        animation.update();

        if (up) {
            dy -= 1;

        } else {
            dy += 1;
        }

        if (dy > 6) dy = 6;
        if (dy < -6) dy = -6;

        y += dy * 2;


        if (powerUpOn) {
            long elapsedPowerUp = (System.nanoTime() - PowerUpTime) / 1000000;
            if (elapsedPowerUp > 2500) {
                setAnimation(spritesheet, 3);
                powerUpOn = false;
            }
        }

    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(animation.getImage(), x, y, null);
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        this.score += score;
    }

    public boolean getPlaying() {
        return playing;
    }

    public void setPlaying(boolean b) {
        playing = b;
    }

    public void resetDY() {
        dy = 0;
    }

    public void resetScore() {
        score = 0;
    }

    public boolean getPowerUpOn() {
        return this.powerUpOn;
    }

    public void PowerUpOn(Bitmap res) {

        powerUpSheet = res;
        setAnimation(powerUpSheet, 3);

        PowerUpTime = System.nanoTime();
        this.powerUpOn = true;

    }
}