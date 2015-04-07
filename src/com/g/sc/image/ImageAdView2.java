package com.g.sc.image;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yyxu.download.utils.BitmapLoader;
import com.yyxu.download.utils.BitmapMemoryHelper;
import com.yyxu.download.utils.BitmapUtil;
import com.yyxu.download.utils.MyIntents;
import com.yyxu.download.utils.StorageUtils;

public class ImageAdView2 extends RelativeLayout implements OnClickListener {

	private TextView text;
	private ProgressBar pb;
	private ImageView image;
	private ImageAdItem item;
	private LayoutParams text_param, pb_param, image_param;
	private MyReceiver receiver;
	private AlertDialog container_dialog;

	public ImageAdView2(Context context) {
		super(context);
		initParam();
	}

	public ImageAdView2(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initParam();
	}

	public ImageAdView2(Context context, AttributeSet attrs) {
		super(context, attrs);
		initParam();
	}

	private void initParam() {
		text_param = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		text_param.addRule(RelativeLayout.CENTER_HORIZONTAL);
		pb_param = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		pb_param.addRule(RelativeLayout.CENTER_IN_PARENT);
		image_param = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		image_param.addRule(RelativeLayout.CENTER_IN_PARENT);
		receiver = new MyReceiver();
		BitmapMemoryHelper.context = getContext();
		setOnClickListener(this);
	}

	public void setContainer_dialog(AlertDialog container_dialog) {
		this.container_dialog = container_dialog;
	}

	public void init(ImageAdItem item) {
		this.item = item;
		text = new TextView(getContext());
		image = new ImageView(getContext());
		text.setText(item.ad_text);
		addView(text, text_param);
		Bitmap bitmap = getItemBitmap();
		if (null != bitmap) {
			image.setImageBitmap(bitmap);
			addView(image, image_param);
		} else {
			pb = new MyProgress(getContext());
			addView(pb, pb_param);
			DownloadSession.getInstance().downloadPic(getContext(), item);
		}
	}

	private void onImageDownloadComplete() {
		if (null != pb && pb.getParent() != null) {
			removeView(pb);
		}
		Bitmap bitmap = getItemBitmap();
		if (null != bitmap)
			image.setImageBitmap(bitmap);
		else
			;
		addView(image, image_param);
	}

	private Bitmap getItemBitmap() {
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		int height = dm.heightPixels;
		int width = dm.widthPixels;
		float height_scale = (float) height / 800.f;
		float width_scale = (float) width / 480.f;
		Bitmap bitmap = BitmapLoader.load(item.getPicPath());
		if (null != bitmap) {
			bitmap = BitmapUtil.scale(bitmap, width_scale, height_scale);
			bitmap = BitmapUtil.getRoundedCornerBitmap(bitmap, 15);
		}
		return bitmap;
	}

	@Override
	protected void onAttachedToWindow() {
		System.out.println("onAttachedToWindow");
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.yyxu.download.progress");
		getContext().registerReceiver(receiver, filter);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		System.out.println("onDetachedFromWindow");
		getContext().unregisterReceiver(receiver);
		if (null != image) {
			BitmapDrawable dr = (BitmapDrawable) image.getDrawable();
			if (null != dr) {
				dr.getBitmap().recycle();
			}
		}
		super.onDetachedFromWindow();
	}

	@Override
	public void onClick(View v) {
		if ("apk".equals(item.link_type)) {
			File f = new File(item.getLinkApkPath());
			if (f.exists()) {
				StorageUtils.installAPK(getContext(), f.getPath());
			} else {
				DownloadSession.getInstance().downloadApk(getContext(), item, false);
			}
		} else {
			Uri uri = Uri.parse(item.pic_link);
			Intent it = new Intent(Intent.ACTION_VIEW, uri);
			it.setClassName("com.android.browser",
					"com.android.browser.BrowserActivity");
			getContext().startActivity(it);
		}
		if (null != container_dialog) {
			container_dialog.dismiss();
		} else {
			((ViewGroup) this.getParent()).removeView(this);
		}
	}

	public class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			handleIntent(intent);
		}

		private void handleIntent(Intent intent) {
			if (intent != null
					&& intent.getAction().equals("com.yyxu.download.progress")) {
				int type = intent.getIntExtra(MyIntents.TYPE, -1);
				String url = intent.getStringExtra(MyIntents.URL);
				System.out.println("type=>" + type + "\nurl=>" + url);
				if (!url.equals(item.pic_url))
					return;
				switch (type) {
				case MyIntents.Types.ADD:
					break;
				case MyIntents.Types.COMPLETE:
					onImageDownloadComplete();
					break;
				case MyIntents.Types.PROCESS:
					String progress = intent
							.getStringExtra(MyIntents.PROCESS_PROGRESS); // int;
					System.out.println("on progress " + progress);
					if (null != pb && null != pb.getParent())
						pb.setProgress(Integer.valueOf(progress));
					break;
				case MyIntents.Types.ERROR:
					break;
				default:
					break;
				}
			}
		}
	}

}
