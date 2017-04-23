package com.onektower.container;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by zhwilson on 2017/4/23.
 * 做一些验证测试以及知识复习
 */
public class ContainerImitate extends ViewGroup{
    private Context mContext;
    public ContainerImitate(Context context) {
        this(context, null);
    }

    public ContainerImitate(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContainerImitate(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.i("zhwilson", "viewGroup init");
        mContext = context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.i("zhwilson", "viewGroup onFinishInflate");
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.i("zhwilson", "viewGroup onMeasure");
        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.i("zhwilson", "viewGroup onSizeChanged");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        Log.i("zhwilson", "viewGroup onLayout");
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++){
            View child = getChildAt(i);
            child.layout(0, 0, child.getMeasuredWidth(), child.getMeasuredHeight());
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.i("zhwilson", "viewGroup dispatchTouchEvent");
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.i("zhwilson", "viewGroup onInterceptTouchEvent");
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("zhwilson", "viewGroup onTouchEvent");
        return super.onTouchEvent(event);
    }
}
