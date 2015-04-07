package com.g.sc.image;

import com.l.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ProgressBar;

public class MyProgress extends ProgressBar {
	String text;
	Paint mPaint;
	Rect rect = new Rect();
	boolean progress_text_enable = false;

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
		if (progress < 0 || progress > 100)
			return;
		setText(progress);
		super.setProgress(progress);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (progress_text_enable) {
			this.mPaint.getTextBounds(this.text, 0, this.text.length(), rect);
			int x = (getWidth() / 2) - rect.centerX();
			int y = (getHeight() / 2) - rect.centerY();
			canvas.drawText(this.text, x, y, this.mPaint);
		}
	}

	public static int dpToPx(int dp, Context ctx) {
		Resources r = ctx.getResources();
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp,
				r.getDisplayMetrics());
	}
	
	public void setEnableProgressText(boolean enable) {
		this.progress_text_enable = enable;
	}

	// 初始化，画笔
	private void initText() {
		this.mPaint = new Paint();
		this.mPaint.setColor(Color.WHITE);
		this.mPaint.setTextSize(dpToPx(14, getContext()));
		this.mPaint.setAntiAlias(true);
		this.setIndeterminateDrawable(getContext().getResources().getDrawable(R.drawable.progress));
	}

	// 设置文字内容
	private void setText(int progress) {
		int i = (progress * 100) / this.getMax();
		this.text = String.valueOf(i) + "%";
	}

}