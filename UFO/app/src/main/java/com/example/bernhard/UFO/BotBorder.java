package com.example.bernhard.UFO;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Bernhard on 26.05.2015.
 */
public class BotBorder extends GameObject {

    private Bitmap image;

<<<<<<< HEAD
    public BotBorder(Bitmap res, int x, int y) {
=======
    public BotBorder(Bitmap res, int x, int y)
    {
>>>>>>> 7dbdfff1fdfa4b0a00c291a66f697516800b67ca
        height = 200;
        width = 20;

        this.x = x;
        this.y = y;
        dx = GamePanel.MOVESPEED;

        image = Bitmap.createBitmap(res, 0, 0, width, height);

    }

    public void update() {
        x += dx;

    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(image, x, y, null);

    }
}
