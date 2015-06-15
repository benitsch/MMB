package com.example.bernhard.UFO;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Bernhard on 24.05.2015.
 */
public class GamePanel extends SurfaceView implements SurfaceHolder.Callback {
    public static final int WIDTH = 856;
    public static final int HEIGHT = 480;
    public static final int MOVESPEED = -5;
    private long powerUpTime;
    private long missileStartTime;
    private MainThread thread;
    private Background bg, bg2;
    private Player player;
    private ArrayList<Smokepuff> smoke;
    private ArrayList<Missile> missiles;
    private ArrayList<TopBorder> topborder;
    private ArrayList<BotBorder> botborder;
    private PowerUp powerUp;
    private Random rand = new Random();
    private int maxBorderHeight;
    private int minBorderHeight;
    private boolean topDown = true;
    private boolean botDown = true;
    private boolean newGameCreated;

    //increase to slow down difficulty progression, decrease to speed up difficulty progression
    private int progressDenom = 20;

    private Explosion explosion;
    private long startReset;
    private boolean reset;
    private boolean dissapear;
    private boolean started;
    private int best = 0;

    private SoundPool sp = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    int soundId = sp.load(getContext(), R.raw.explosion, 1);
    int soundId2 = sp.load(getContext(), R.raw.explosionfail, 1);
    int soundId3 = sp.load(getContext(), R.raw.powerup, 1);


    private SoundPool sp2= new SoundPool(1,AudioManager.STREAM_MUSIC,0);
    int soundId4 = sp2.load(getContext(), R.raw.tot2, 1);


    public GamePanel(Context context) {
        super(context);


        //add the callback to the surfaceholder to intercept events
        getHolder().addCallback(this);


        //make gamePanel focusable so it can handle events
        setFocusable(true);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        int counter = 0;
        while (retry && counter < 1000) {
            counter++;
            try {
                thread.setRunning(false);
                thread.join();
                retry = false;
                thread = null;

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        bg = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background2));
        bg.setVector(-1);
        bg2 = new Background(BitmapFactory.decodeResource(getResources(), R.drawable.background3));
        bg2.setVector(-5);

        player = new Player(BitmapFactory.decodeResource(getResources(), R.drawable.ufo), 66, 40, 3);
        missiles = new ArrayList<Missile>();
        topborder = new ArrayList<TopBorder>();
        botborder = new ArrayList<BotBorder>();
        powerUpTime = System.nanoTime();
        missileStartTime = System.nanoTime();

        thread = new MainThread(getHolder(), this);
        //we can safely start the game loop
        thread.setRunning(true);
        thread.start();

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (!player.getPlaying() && newGameCreated && reset) {
                player.setPlaying(true);
                player.setUp(true);
            }
            if (player.getPlaying()) {

                if (!started) started = true;
                reset = false;
                player.setUp(true);
            }
            return true;
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            player.setUp(false);
            return true;
        }

        return super.onTouchEvent(event);
    }

    public void update()

    {
        if (player.getPlaying()) {

            if (botborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }
            if (topborder.isEmpty()) {
                player.setPlaying(false);
                return;
            }

            bg.update();
            bg2.update();
            player.update();

            //calculate the threshold of height the border can have based on the score
            //max and min border heart are updated, and the border switched direction when either max or
            //min is met

            maxBorderHeight = 30 + player.getScore() / progressDenom;
            //cap max border height so that borders can only take up a total of 1/2 the screen
            if (maxBorderHeight > HEIGHT / 4) maxBorderHeight = HEIGHT / 4;
            minBorderHeight = 5 + player.getScore() / progressDenom;

            //check bottom border collision
            for (int i = 0; i < botborder.size(); i++) {
                if ((collision(botborder.get(i), player) || playerOutOfScreen()) && !player.getPowerUpOn()) {
                    updateBest();
                    soundExplosion();
                    soundTot();
                    player.setPlaying(false);
                }
            }

            //check top border collision
            for (int i = 0; i < topborder.size(); i++) {
                if ((collision(topborder.get(i), player) || playerOutOfScreen()) && !player.getPowerUpOn()) {
                    updateBest();
                    soundExplosion();
                    soundTot();
                    player.setPlaying(false);
                }
            }

            //update top border
            this.updateTopBorder();

            //udpate bottom border
            this.updateBottomBorder();

            //add missiles on timer
            long missileElapsed = (System.nanoTime() - missileStartTime) / 1000000;
            if (missileElapsed > (2000 - player.getScore() / 4)) {

                //first missile always goes down the middle
                if (missiles.size() == 0) {
                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.
                            missile), WIDTH + 10, HEIGHT / 2, 45, 15, player.getScore(), 13));
                } else {

                    missiles.add(new Missile(BitmapFactory.decodeResource(getResources(), R.drawable.missile),
                            WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight), 45, 15, player.getScore(), 13));
                }

                //reset timer
                missileStartTime = System.nanoTime();
            }
            //loop through every missile and check collision and remove
            for (int i = 0; i < missiles.size(); i++) {
                //update missile
                missiles.get(i).update();

                if (collision(missiles.get(i), player)) {
                    missiles.remove(i);


                    if (!player.getPowerUpOn()) {
                        updateBest();
                        soundExplosion();
                        soundTot();
                        player.setPlaying(false);
                    } else {
                        soundFailedExplosion();
                        player.addScore(50);
                    }

                    break;
                }


                //remove missile if it is way off the screen
                if (missiles.get(i).getX() < -100) {
                    missiles.remove(i);
                    break;
                }


                //add powerUps on timer
                long elapsed = (System.nanoTime() - powerUpTime) / 1000000;
                if (elapsed > 120) {
                    if (powerUp == null) {
                        //if (rand.nextDouble() >= .0) TODO less often?
                        powerUp = new PowerUp(BitmapFactory.decodeResource(getResources(), R.drawable.powerup),
                                WIDTH + 10, (int) (rand.nextDouble() * (HEIGHT - (maxBorderHeight * 2)) + maxBorderHeight), 45, 15, player.getScore(), 13);

                    }
                    if (powerUp != null) {
                        //update powerUp
                        powerUp.update();

                        //powerUp check collision and remove
                        if (collision(powerUp, player)) {
                            powerUp = null;
                            soundPowerUp();
                            player.addScore(25);
                            player.PowerUpOn(BitmapFactory.decodeResource(getResources(), R.drawable.ufo2));
                        }

                        //remove powerUp if is is way off the screen
                        else if (powerUp.getX() < -100) {
                            powerUp = null;
                        }
                    }
                }
            }


        } else {
            player.resetDY();
            if (!reset) {
                newGameCreated = false;
                startReset = System.nanoTime();
                reset = true;
                dissapear = true;
                explosion = new Explosion(BitmapFactory.decodeResource(getResources(), R.drawable.explosion), player.getX(),
                        player.getY() - 30, 100, 100, 25);
            }

            explosion.update();
            long resetElapsed = (System.nanoTime() - startReset) / 1000000;

            if (resetElapsed > 2500 && !newGameCreated) {
                newGame();

            }

        }


    }

    private void updateBest() {
        if (player.getScore() > best) {
            best = player.getScore();
        }
    }

    private void soundExplosion() {
        sp.play(soundId, .4f, .4f, 0, 0, 1);
    }

    private void soundFailedExplosion() {
        sp.play(soundId2, .9f, .9f, 0, 0, 1);

    }

    private void soundPowerUp() {
        sp.play(soundId3, .5f, .5f, 0, 0, 1);

    }

    private void soundTot(){
        sp2.play(soundId4, .9f, .9f, 0, 0, 1);
    }

    public boolean playerOutOfScreen() {
        if (player.getY() < 0 || player.getY() > HEIGHT)
            return true;
        return false;
    }

    public boolean collision(GameObject a, GameObject b) {
        if (Rect.intersects(a.getRectangle(), b.getRectangle())) {
            return true;
        }

        return false;
    }

    @Override
    public void draw(Canvas canvas) {
        final float scaleFactorX = getWidth() / (WIDTH * 1.f);
        final float scaleFactorY = getHeight() / (HEIGHT * 1.f);

        if (canvas != null) {
            final int savedState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);
            bg.draw(canvas);
            bg2.draw(canvas);

            if (!dissapear) {
                player.draw(canvas);
            }

            //draw powerup
            if (powerUp != null)
                powerUp.draw(canvas);

            //draw missiles
            for (Missile m : missiles) {
                m.draw(canvas);
            }


            //draw topborder
            for (TopBorder tb : topborder) {
                tb.draw(canvas);
            }

            //draw botborder
            for (BotBorder bb : botborder) {
                bb.draw(canvas);
            }
            //draw explosion
            if (started) {
                explosion.draw(canvas);
            }
            drawText(canvas);
            canvas.restoreToCount(savedState);

        }
    }

    public void updateTopBorder() {
        //every 50 points, insert randomly placed top blocks that break the pattern
        if (player.getScore() % 50 == 0) {
            topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
            ), topborder.get(topborder.size() - 1).getX() + 20, 0, (int) ((rand.nextDouble() * (maxBorderHeight
            )) + 1)));
        }
        for (int i = 0; i < topborder.size(); i++) {
            topborder.get(i).update();
            if (topborder.get(i).getX() < -20) {
                topborder.remove(i);
                //remove element of arraylist, replace it by adding a new one

                //calculate topdown which determines the direction the border is moving (up or down)
                if (topborder.get(topborder.size() - 1).getHeight() >= maxBorderHeight) {
                    topDown = false;
                }
                if (topborder.get(topborder.size() - 1).getHeight() <= minBorderHeight) {
                    topDown = true;
                }
                //new border added will have larger height
                if (topDown) {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size() - 1).getX() + 20,
                            0, topborder.get(topborder.size() - 1).getHeight() + 1));
                }
                //new border added wil have smaller height
                else {
                    topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(),
                            R.drawable.brick), topborder.get(topborder.size() - 1).getX() + 20,
                            0, topborder.get(topborder.size() - 1).getHeight() - 1));
                }

            }
        }

    }

    public void updateBottomBorder() {
        //every 40 points, insert randomly placed bottom blocks that break pattern
        if (player.getScore() % 40 == 0) {
            botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                    botborder.get(botborder.size() - 1).getX() + 20, (int) ((rand.nextDouble()
                    * maxBorderHeight) + (HEIGHT - maxBorderHeight))));
        }

        //update bottom border
        for (int i = 0; i < botborder.size(); i++) {
            botborder.get(i).update();

            //if border is moving off screen, remove it and add a corresponding new one
            if (botborder.get(i).getX() < -20) {
                botborder.remove(i);


                //determine if border will be moving up or down
                if (botborder.get(botborder.size() - 1).getY() <= HEIGHT - maxBorderHeight) {
                    botDown = true;
                }
                if (botborder.get(botborder.size() - 1).getY() >= HEIGHT - minBorderHeight) {
                    botDown = false;
                }

                if (botDown) {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() + 1));
                } else {
                    botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                    ), botborder.get(botborder.size() - 1).getX() + 20, botborder.get(botborder.size() - 1
                    ).getY() - 1));
                }
            }
        }
    }

    public void newGame() {
        dissapear = false;

        botborder.clear();
        topborder.clear();

        missiles.clear();
        // smoke.clear();

        minBorderHeight = 5;
        maxBorderHeight = 30;

        player.resetDY();
        player.resetScore();
        player.setY(HEIGHT / 2);
        powerUp = null;


        //create initial borders

        //initial top border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            //first top border create
            if (i == 0) {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i * 20, 0, 10));
            } else {
                topborder.add(new TopBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick
                ), i * 20, 0, topborder.get(i - 1).getHeight() + 1));
            }
        }
        //initial bottom border
        for (int i = 0; i * 20 < WIDTH + 40; i++) {
            //first border ever created
            if (i == 0) {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick)
                        , i * 20, HEIGHT - minBorderHeight));
            }
            //adding borders until the initial screen is filed
            else {
                botborder.add(new BotBorder(BitmapFactory.decodeResource(getResources(), R.drawable.brick),
                        i * 20, botborder.get(i - 1).getY() - 1));
            }
        }

        newGameCreated = true;


    }

    public void drawText(Canvas canvas) {
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("DISTANCE: " + (player.getScore()), 10, HEIGHT - 10, paint);
        canvas.drawText("BEST: " + best, WIDTH - 215, HEIGHT - 10, paint);

        if (!player.getPlaying() && newGameCreated && reset) {
            Paint paint1 = new Paint();
            paint1.setColor(Color.WHITE);
            paint1.setTextSize(40);
            paint1.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            canvas.drawText("PRESS TO START", WIDTH / 2 - 50, HEIGHT / 2, paint1);

            paint1.setTextSize(20);
            canvas.drawText("PRESS AND HOLD TO GO UP", WIDTH / 2 - 50, HEIGHT / 2 + 20, paint1);
            canvas.drawText("RELEASE TO GO DOWN", WIDTH / 2 - 50, HEIGHT / 2 + 40, paint1);
        }
    }
}