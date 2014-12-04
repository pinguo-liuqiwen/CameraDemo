package com.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Scroller;

import com.camera.config.Util;


/**
 * SeekBar,支持中空的thumb;
 * 支持横向和竖向，默认竖向，使用setOrientation改变方向;
 * 使用LinePaint1、LinePaint2、ThumbPaint控制绘制参数;
 */
public class SeekBar extends View {

    //seek parameters
    protected float mSeekRate;
    protected int mSeekLength;
    protected int mSeekLineStart;
    protected int mSeekLineEnd;
    private OnSeekChangedListener mListener;
    private OnDrawListener mOnDrawListener;
    //thumb parameters
    protected Paint mThumbPaint;
    protected int mThumbOffset;
    protected int mThumbRadius = Util.dpToPixel(12);
    protected int mLargeThumbRadius = (int) (mThumbRadius * 1.5f);
    protected int mThumbStorkeWidth = Math.round(Util.dpToPixel(1.5f));
    protected Drawable mThumbDrawable;
    //line parameters
    protected Paint mLinePaint1;
    protected Paint mLinePaint2;
    protected int mLineWidth = Math.round(Util.dpToPixel(1.5f));
    //touch parameters
    protected GestureDetector mGestureDetector;
    //animation parameters
    protected Scroller mScroller;
    //orientation parameters
    private boolean mIsVertical = false;

    protected boolean mDrawLargeCircle = false;

    public SeekBar(Context context) {
        this(context, null);
    }

    public SeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mScroller = new Scroller(getContext());
        mGestureDetector = new GestureDetector(getContext(), new SeekBarGestureListener());
        mThumbPaint = new Paint();
        mThumbPaint.setAntiAlias(true);
        mThumbPaint.setColor(Color.WHITE);
        mThumbPaint.setStrokeWidth(mThumbStorkeWidth);
        mThumbPaint.setStyle(Paint.Style.STROKE);
        mLinePaint1 = new Paint();
        mLinePaint1.setAntiAlias(true);
        mLinePaint1.setColor(Color.WHITE);
        mLinePaint1.setAlpha(200);
        mLinePaint2 = new Paint();
        mLinePaint2.setAntiAlias(true);
        mLinePaint2.setColor(Color.WHITE);
        mLinePaint2.setAlpha(200);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                mDrawLargeCircle = true;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDrawLargeCircle = false;
                invalidate();
                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mIsVertical) {
            int wmode = MeasureSpec.getMode(widthMeasureSpec);
            int wsize;
            int hsize = MeasureSpec.getSize(heightMeasureSpec);
            if (wmode == MeasureSpec.AT_MOST) {
                wsize = (mThumbRadius + mThumbStorkeWidth) * 2 > mLineWidth ? (mThumbRadius + mThumbStorkeWidth) * 2 : mLineWidth;
                if (mThumbDrawable != null) {
                    wsize = wsize > mThumbDrawable.getIntrinsicWidth() ? wsize : mThumbDrawable.getIntrinsicWidth();
                }
                wsize += getPaddingLeft() + getPaddingRight();
                setMeasuredDimension(wsize, hsize);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        } else {
            int hmode = MeasureSpec.getMode(heightMeasureSpec);
            int hsize;
            int wsize = MeasureSpec.getSize(widthMeasureSpec);
            if (hmode == MeasureSpec.AT_MOST) {
                hsize = (mThumbRadius + mThumbStorkeWidth) * 2 > mLineWidth ? (mThumbRadius + mThumbStorkeWidth) * 2 : mLineWidth;
                if (mThumbDrawable != null) {
                    hsize = hsize > mThumbDrawable.getIntrinsicHeight() ? hsize : mThumbDrawable.getIntrinsicHeight();
                }
                hsize += getPaddingTop() + getPaddingBottom();
                setMeasuredDimension(wsize, hsize);
            } else {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mSeekLength == 0) {
            //初始化参数
            if (mIsVertical) {
                final int height = getHeight();
                mSeekLength = height - getPaddingTop() - getPaddingBottom() - mThumbRadius * 2 - mThumbStorkeWidth * 2;
                mSeekLineStart = getPaddingTop() + mThumbRadius + mThumbStorkeWidth;
                mSeekLineEnd = height - getPaddingBottom() - mThumbRadius - mThumbStorkeWidth;
            } else {
                final int width = getWidth();
                mSeekLength = width - getPaddingLeft() - getPaddingRight() - mThumbRadius * 2 - mThumbStorkeWidth * 2;
                mSeekLineStart = getPaddingLeft() + mThumbRadius + mThumbStorkeWidth;
                mSeekLineEnd = width - getPaddingRight() - mThumbRadius - mThumbStorkeWidth;
            }
            mThumbOffset = (int) (mSeekLength * mSeekRate);
        }
        //draw line
        if (mIsVertical) {
            final int left = getPaddingLeft() + mThumbRadius + mThumbStorkeWidth / 2 - mLineWidth / 2;
            final int right = getPaddingLeft() + mLineWidth + mThumbRadius + mThumbStorkeWidth / 2 - mLineWidth / 2;
            final int bottom1 = mSeekLineEnd - mThumbOffset + mThumbStorkeWidth / 2 - mThumbRadius;
            if (bottom1 > mSeekLineStart) {
                canvas.drawRect(left, mSeekLineStart, right, bottom1, mLinePaint2);
            }
            final int top2 = bottom1 + mThumbRadius * 2;
            if (mSeekLineEnd > top2) {
                canvas.drawRect(left, top2, right, mSeekLineEnd, mLinePaint1);
            }


        } else {
            final int top = getPaddingTop() + mThumbRadius + mThumbStorkeWidth / 2 - mLineWidth / 2;
            final int bottom = getPaddingTop() + mLineWidth + mThumbRadius + mThumbStorkeWidth / 2 - mLineWidth / 2;
            final int right1 = mSeekLineStart + mThumbOffset + mThumbStorkeWidth / 2 - mThumbRadius;

            if (right1 > mSeekLineStart) {
                canvas.drawRect(mSeekLineStart, top, right1, bottom, mLinePaint1);
            }
            final int left2 = right1 + mThumbRadius * 2;
            if (mSeekLineEnd > left2) {
                canvas.drawRect(left2, top, mSeekLineEnd, bottom, mLinePaint2);
            }

            if (mOnDrawListener != null) {
                mOnDrawListener.onHorizontalDrawLineFinish(canvas, mSeekLineStart, right1, left2, mSeekLineEnd);
            }
        }
        //draw thumb
        final int thumbX;
        final int thumbY;
        if (mIsVertical) {
            thumbX = mThumbRadius + mThumbStorkeWidth / 2 + getPaddingLeft();
            thumbY = mSeekLineEnd - mThumbOffset;
        } else {
            thumbY = mThumbRadius + mThumbStorkeWidth / 2 + getPaddingTop();
            thumbX = mSeekLineStart + mThumbOffset;
        }
        canvas.drawCircle(thumbX, thumbY, mThumbRadius, mThumbPaint);
        if (mThumbDrawable != null) {
            int tdWidth = mThumbDrawable.getIntrinsicWidth();
            int tdHeight = mThumbDrawable.getIntrinsicHeight();
            final int thumbDrawableLeft = thumbX - tdWidth / 2;
            final int thumbDrawableTop = thumbY - tdHeight / 2;
            final int thumbDrawableRight = thumbX + tdWidth / 2;
            final int thumbDrawableBottom = thumbY + tdHeight / 2;
            mThumbDrawable.setBounds(thumbDrawableLeft, thumbDrawableTop, thumbDrawableRight, thumbDrawableBottom);
            mThumbDrawable.draw(canvas);
        }
        if (mScroller.computeScrollOffset()) {
            mThumbOffset = mScroller.getCurrY();
            invalidate();
        }
        super.onDraw(canvas);
    }

    private int getThumbOffset(float pos) {
        int thumb = (int) pos;
        if (thumb < 0) {
            thumb = 0;
        } else if (thumb > mSeekLength) {
            thumb = mSeekLength;
        }
        return thumb;
    }

    /**
     * 设置滑动监听
     *
     * @param listener
     */
    public void setOnSeekChangedListener(OnSeekChangedListener listener) {
        mListener = listener;
    }

    public void setmOnDrawListener(OnDrawListener listener) {
        this.mOnDrawListener = listener;
    }

    public float getCurrentSeekValue() {
        return mSeekRate;
    }

    public int getProgress() {
        return Math.round(mSeekRate * 100);
    }

    public void setProgress(int progress) {
        setCurrentSeekValue(progress / 100f);
    }

    /**
     * 设置当前Seek数值
     *
     * @param currentValue from[0..1]
     */
    public void setCurrentSeekValue(float currentValue) {

        if (mSeekRate > 1) {
            mSeekRate = 1;
        } else if (mSeekRate < 0) {
            mSeekRate = 0;
        }
        mSeekRate = currentValue;
        mThumbOffset = (int) (mSeekLength * mSeekRate);
        invalidate();
    }

    /**
     * 设置SeekBar方向，默认为vertical
     */
    public void setOrientation(boolean isVertical) {
        mIsVertical = isVertical;
        requestLayout();
    }

    public Paint getLinePaint1() {
        return mLinePaint1;
    }

    public void setThumbRadius(int thumbRadius) {
        this.mThumbRadius = Util.dpToPixel(thumbRadius);
    }

    public int getThumbRadius() {
        return mThumbRadius;
    }

    public interface OnSeekChangedListener {
        /**
         * @param rate from[0..1]
         */
        void onSeekValueChanged(float rate);
    }

    public static interface OnDrawListener {

        void onHorizontalDrawLineFinish(Canvas canvas, int left1, int right1, int left2, int right2);


    }


    private class SeekBarGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2,
                                float distanceX, float distanceY) {
            if (mIsVertical) {
                float ey = e2.getY();
                mThumbOffset = getThumbOffset(mSeekLineEnd - ey);
            } else {
                float ex = e2.getX();
                mThumbOffset = getThumbOffset(ex - mSeekLineStart);
            }
            if (mListener != null && mSeekLength != 0) {
                mSeekRate = (float) mThumbOffset / mSeekLength;
                mListener.onSeekValueChanged(mSeekRate);
            }
            invalidate();
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            final int offsetLength;
            if (mIsVertical) {
                offsetLength = getThumbOffset(mSeekLineEnd - e.getY());
                mScroller.startScroll(0, mThumbOffset, 0, offsetLength - mThumbOffset, 400);
            } else {
                offsetLength = getThumbOffset(e.getX() - mSeekLineStart);
                mScroller.startScroll(0, mThumbOffset, 0, offsetLength - mThumbOffset, 400);
            }
            if (mListener != null && mSeekLength != 0) {
                mSeekRate = (float) offsetLength / mSeekLength;
                mListener.onSeekValueChanged(mSeekRate);
            }
            invalidate();

            return true;
        }
    }
}

