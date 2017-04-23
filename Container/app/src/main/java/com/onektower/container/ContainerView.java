package com.onektower.container;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * Created by zhwilson on 2017/4/21.
 * 实现3d效果的旋转容器
 */
public class ContainerView extends ViewGroup {
    private Context mContext;
    private Camera mCamera;//相机
    private Matrix mMatrix;//变换矩阵
    private Scroller mScroller;//控制滚动
    private int mWidth, mHeight;//控件的大小
    private int mStartPosition = 1;//开始滑动位置
    private VelocityTracker mVelocityTracker;
    private float mDownY;//当前按下位置（纵坐标）
    private float mDownX;//当前按下位置（横坐标）
    private int mStanderSpeed = 2000;//标准速度，超过会触发滚动
    private int mFlingSpeed = 800;//滚动速度
    private int addCount;//计数增加页面次数
    private int alreadyAddCount;//计数已经增加页面次数
    private int mTouchSlop;//认为是滑动的最小距离
    private boolean isSliding = false;//是否在滑动
    private float resistance = 1.8f;//滑动阻力
    private boolean isAdding = false;//fling时正在添加新页面，在绘制时不需要开启camera绘制效果，否则页面会有闪动
    private State mState;

    public ContainerView(Context context) {
        this(context, null);
    }

    public ContainerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContainerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    private void init() {
        mTouchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
        mCamera = new Camera();
        mMatrix = new Matrix();
        if (mScroller == null)
            mScroller = new Scroller(mContext);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        mWidth = getMeasuredWidth();
        mHeight = getMeasuredHeight();
        scrollTo(0, mStartPosition * mHeight);
        Log.d("zhwilson", "onMeasure控件尺寸:" + getMeasuredWidth() + "-" + getMeasuredHeight());
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
//        mWidth = w;
//        mHeight = h;
//        scrollTo(0, mStartPosition * mHeight);
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {
        int childTop = 0;
        int childCount = getChildCount();
        for (int j = 0; j < childCount; j++) {
            View child = getChildAt(j);
            if (View.VISIBLE != child.getVisibility())
                continue;
            child.layout(0, childTop, child.getMeasuredWidth(), childTop + child.getMeasuredHeight());
            childTop = childTop + child.getMeasuredHeight();
        }
    }

    private float mTempY;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        float touchX = ev.getX();
        float touchY = ev.getY();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                isSliding = false;
                mDownX = touchX;
                mTempY = mDownY = touchY;
                if (!mScroller.isFinished()) {
                    //正在滑动，强制停止在当前点击位置
                    mScroller.setFinalY(mScroller.getCurrY());
                    mScroller.abortAnimation();
                    scrollTo(0, getScrollY());
                    isSliding = true;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isSliding)
                    isSliding = isCanSliding(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private boolean isCanSliding(MotionEvent event) {
        float moveX;
        float moveY;
        moveX = event.getX();
        mTempY = moveY = event.getY();
        //TODO ?? 原文是Math.abs(moveY - mDownX) > mTouchSlop
        if (Math.abs(moveY - mDownY) > mTouchSlop && (Math.abs(moveY - mDownY) > Math.abs(moveX - mDownX))) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isSliding;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null)
            mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);
        float touchY = event.getY();
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isSliding) {
                    int realDelta = (int) (mDownY - touchY);
                    mDownY = touchY;
                    if (mScroller.isFinished()) {
                        //TODO ??
                        recycleMove(realDelta);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (isSliding) {
                    isSliding = false;
                    mVelocityTracker.computeCurrentVelocity(1000);
                    float yVelocity = mVelocityTracker.getYVelocity();
                    //一些是判读是否滚动，怎么滚动
                    if (yVelocity > mStanderSpeed || (getScrollY() + mHeight / 2) / mHeight < mStartPosition)
                        mState = State.ToPre;
                    else if (yVelocity < -mStanderSpeed || (getScrollY() + mHeight / 2) / mHeight > mStartPosition)
                        mState = State.ToNext;
                    else mState = State.Normal;
                    scrollByState(yVelocity);
                    if (mVelocityTracker != null) {
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                }
                break;
        }
        return super.onTouchEvent(event);
    }

    private void scrollByState(float velocity) {
        alreadyAddCount = 0;
        if (getScrollY() != mHeight) {
            switch (mState) {
                case ToPre:
                    toPreAction(velocity);
                    break;
                case ToNext:
                    toNextAction(velocity);
                    break;
                case Normal:
                    toNormalAction();
                    break;
            }
            invalidate();
        }
    }

    private void toNormalAction() {
        int startY;
        int delta;
        int duration;
        mState = State.Normal;
        addCount = 0;
        startY = getScrollY();
        delta = mHeight * mStartPosition - getScrollY();
        duration = (Math.abs(delta)) * 4;
        mScroller.startScroll(0, startY, 0, delta, duration);
    }

    private void toPreAction(float velocity) {
        int startY;
        int delta;
        int duration;
        mState = State.ToPre;
        addPre();
        //计算需要滑动的item个数
        int flingCount = (velocity - mStanderSpeed) > 0 ? (int) (velocity - mStanderSpeed) : 0;
        addCount = flingCount / mFlingSpeed + 1;
        startY = getScrollY() + mHeight;
        setScaleY(startY);
        delta = -(startY - mStartPosition * mHeight) - (addCount - 1) * mHeight;
        duration = (Math.abs(delta)) * 3;
        mScroller.startScroll(0, startY, 0, delta, duration);
        addCount--;
    }

    private void toNextAction(float velocity) {
        int startY;
        int delta;
        int duration;
        mState = State.ToNext;
        addNext();
        int flingCount = (Math.abs(velocity) - mStanderSpeed) > 0 ? (int) (Math.abs(velocity) - mStanderSpeed) : 0;
        addCount = flingCount / mFlingSpeed + 1;
        startY = getScrollY() - mHeight;
        setScaleY(startY);
        delta = mHeight * mStartPosition - startY + (addCount - 1) * mHeight;
        duration = (Math.abs(delta)) * 3;
        mScroller.startScroll(0, startY, 0, delta, duration);
        addCount--;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            if (mState == State.ToPre) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY() + mHeight * alreadyAddCount);
                if (getScrollY() < (mHeight + 2) && addCount > 0) {
                    isAdding = true;
                    addPre();
                    alreadyAddCount++;
                    addCount--;
                }
            } else if (mState == State.ToNext) {
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY() - mHeight * alreadyAddCount);
                if (getScrollY() > (mHeight) && addCount > 0) {
                    isAdding = true;
                    addNext();
                    addCount--;
                    alreadyAddCount++;
                }
            } else {
                //mState == State.Normal状态
                scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            }
            postInvalidate();
        }
        //滑动结束时相关用于计数变量复位
        if (mScroller.isFinished()) {
            alreadyAddCount = 0;
            addCount = 0;
        }
    }

    private int mCurrentPosition;

    /**
     * 将最后一个控件移动第一个位置
     */
    private void addPre() {
        int childCount = getChildCount();
        mCurrentPosition = ((mCurrentPosition - 1) + childCount) % childCount;
        View child = getChildAt(childCount - 1);
        removeViewAt(childCount - 1);
        addView(child, 0);
        //TODO 这里设置了一个监听
    }

    private void addNext() {
        int childCount = getChildCount();
        mCurrentPosition = (mCurrentPosition + 1) % childCount;
        View child = getChildAt(0);
        removeViewAt(0);
        addView(child, childCount - 1);
        //TODO 这里设置了一个监听
    }

    private void recycleMove(int delta) {
        delta = delta % mHeight;
        delta = (int) (delta / resistance);
        //TODO ??  这里是move的增量，如果两次之间的增量很大，说明滑动很快，这里就需要在motionEvent.up里面做处理了
        if (Math.abs(delta) > mHeight / 4)
            return;
        scrollBy(0, delta);
        if (getScrollY() < 5 && mCurrentPosition != 0) {
            addPre();
            scrollBy(0, mHeight);
        } else if (getScrollY() > (getChildCount() - 1) * mHeight - 5) {
            addNext();
            scrollBy(0, -mHeight);
        }
    }

    public void setmStartPosition(int mStartPosition) {
        this.mStartPosition = mStartPosition;
    }

    //滚动状态
    public enum State {
        ToPre, ToNext, Normal
    }
}
