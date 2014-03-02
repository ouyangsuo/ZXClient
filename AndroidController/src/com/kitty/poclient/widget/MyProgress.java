package com.kitty.poclient.widget;

import com.kitty.poclient.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class MyProgress extends ProgressBar {
    String text;
    Paint mPaint;
     
    public MyProgress(Context context) {
        super(context);
        initText();
    }
     
    public MyProgress(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initText();
    }
 
 
    public MyProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        initText();
    }
     
    @Override
    public synchronized void setProgress(int progress) {
        setText(progress);
        super.setProgress(progress);
         
    }
 
    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect rect = new Rect();
        this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
        int x = (getWidth() / 2) - rect.centerX(); 
        int y = (getHeight() / 2) - rect.centerY(); 
        canvas.drawText(this.text, x, y, this.mPaint); 
    }
     
    //初始化，画笔
    private void initText(){
        this.mPaint = new Paint();
        this.mPaint.setColor(Color.WHITE);
        this.mPaint.setTextSize(20);
        this.mPaint.setTextSize(getResources().getInteger(R.integer.home_progress_textsize));
    }
     
    private void setText(){
        setText(this.getProgress());
    }
     
    //设置文字内容
    private void setText(int progress){
        int i = (progress * 100)/this.getMax();
        this.text = String.valueOf(i) + "%";
    }
     
}
