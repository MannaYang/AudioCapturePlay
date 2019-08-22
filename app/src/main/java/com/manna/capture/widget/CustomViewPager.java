package com.manna.capture.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 去除ViewPager切换动画
 * */
public class CustomViewPager extends ViewPager {

    private boolean isCanScroll = true;

    public CustomViewPager(Context context) {
        super(context);
    }

    public CustomViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setCurrentItem(int item) { // false 去掉切换动画
        super.setCurrentItem(item, false);
    }

    @Override
    public void setCurrentItem(int item, boolean smoothScroll) {
        super.setCurrentItem(item, smoothScroll);
    }

    public void setCanScroll(boolean isCanScroll) {
        this.isCanScroll = isCanScroll;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return isCanScroll && super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        return isCanScroll && super.onTouchEvent(ev);
    }
}