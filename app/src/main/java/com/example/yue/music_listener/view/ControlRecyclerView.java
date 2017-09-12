package com.example.yue.music_listener.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yue on 2017/9/9.
 */

public class ControlRecyclerView extends RecyclerView {
    private boolean canScroll;
    public ControlRecyclerView(Context context) {
        super(context);
    }

    public ControlRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if(canScroll) {
            return super.onTouchEvent(e);
        }else {
            return false;
        }
    }

    public void setCanScroll(boolean canScroll) {
        this.canScroll = canScroll;
    }
}
