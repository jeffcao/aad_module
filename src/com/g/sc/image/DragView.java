package com.g.sc.image;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class DragView implements OnTouchListener {
	int lastX, lastY;
	int screenHeight = 800, screenWidth = 480;
	long last_action_down = -1;
	private static final long CLICK_INTERVAL = 200;
	private static final boolean ALWAYS_CLICK = false;

	public void init(Context context) {
		DisplayMetrics dm = context.getResources().getDisplayMetrics();
		screenWidth = dm.widthPixels;
		screenHeight = dm.heightPixels;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			last_action_down = System.currentTimeMillis();
			break;
		/**
		 * layout(l,t,r,b) l Left position, relative to parent t Top position,
		 * relative to parent r Right position, relative to parent b Bottom
		 * position, relative to parent
		 * */
		case MotionEvent.ACTION_MOVE:
			int dx = (int) event.getRawX() - lastX;
			int dy = (int) event.getRawY() - lastY;

			int left = v.getLeft() + dx;
			int top = v.getTop() + dy;
			int right = v.getRight() + dx;
			int bottom = v.getBottom() + dy;
			if (left < 0) {
				left = 0;
				right = left + v.getWidth();
			}
			if (right > screenWidth) {
				right = screenWidth;
				left = right - v.getWidth();
			}
			if (top < 0) {
				top = 0;
				bottom = top + v.getHeight();
			}
			if (bottom > screenHeight) {
				bottom = screenHeight;
				top = bottom - v.getHeight();
			}
			v.layout(left, top, right, bottom);
			lastX = (int) event.getRawX();
			lastY = (int) event.getRawY();
			break;
		case MotionEvent.ACTION_UP:
			if (System.currentTimeMillis() - last_action_down > CLICK_INTERVAL) {
				return !ALWAYS_CLICK;
			}
			break;
		}
		return false;
	}

}
