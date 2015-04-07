package com.g.sc.image;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MsgProgressBar extends RelativeLayout {
	MyProgress progress_bar;
	TextView msg_text_view;
	public MsgProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public MsgProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public MsgProgressBar(Context context) {
		super(context);
		init();
	}
	
	public MyProgress getProgress_bar() {
		return progress_bar;
	}

	public TextView getMsg_text_view() {
		return msg_text_view;
	}

	private void init() {
		int progress_id = 111;
		LayoutParams progress_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		progress_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
		progress_bar = new MyProgress(getContext());
		progress_bar.setId(progress_id);
		msg_text_view = new TextView(getContext());
		msg_text_view.setTextColor(getContext().getResources().getColor(android.R.color.white));
		msg_text_view.setText("正在加载...");
		LayoutParams text_param = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		text_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
		text_param.addRule(RelativeLayout.BELOW, progress_id);
		addView(progress_bar, progress_param);
		addView(msg_text_view, text_param);
	}

}
