package com.g.sc.image;

import android.content.Context;
import android.widget.RelativeLayout;

public class ProgressDialog extends BaseDialog {
	private MsgProgressBar progress_bar;

	public ProgressDialog(Context context) {
		super(context);
		progress_bar = new MsgProgressBar(context);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_IN_PARENT);
		progress_bar.setLayoutParams(params);
		setContentView(progress_bar);
	}

	public void setEnableProgressText(boolean enable) {
		progress_bar.getProgress_bar().setEnableProgressText(enable);
	}

	public void setMsg(String msg) {
		progress_bar.getMsg_text_view().setText(msg);
	}

}
