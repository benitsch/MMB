package com.example.bernhard.UFO;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Bernhard on 24.05.2015.
 */
public class Background {
    private Bitmap image;
    private int x, y, dx;

    public Background(Bitmap res)
    {
        image = res;
    }
    public void update()
    {
        x+=dx;
        if(x<-GamePanel.WIDTH){
            x=0;
        }
    }
    public void draw(Canvas canvas)
    {
        //drawBitmap(Bitmap bitmap, float left, float top, Paint paint)
        canvas.drawBitmap(image, x, y,null);
        if(x<0)
        {
            canvas.drawBitmap(image, x+GamePanel.WIDTH, y, null);
        }
    }
    public void setVector(int dx)
    {
        this.dx = dx;
    }
}
