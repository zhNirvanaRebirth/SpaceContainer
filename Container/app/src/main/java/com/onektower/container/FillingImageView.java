package com.onektower.container;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

/**
 * Created by zhwilson on 2017/4/23.
 * 自定义ImageView，用于验证事件分发及相关生命周期
 */
public class FillingImageView extends ImageView{
    public FillingImageView(Context context) {
        this(context, null);
    }

    public FillingImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FillingImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d("zhwilson", "ImageView init");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d("zhwilson", "ImageView onFinishInflate");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("zhwilson", "ImageView onMeasure");
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d("zhwilson", "ImageView onSizeChanged");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d("zhwilson", "ImageView onDraw");
        canvas.drawColor(Color.YELLOW);
    }

    //事件分发相关

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Log.d("zhwilson", "ImageView dispatchTouchEvent");
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("zhwilson", "ImageView onTouchEvent");
        return super.onTouchEvent(event);
    }
}
