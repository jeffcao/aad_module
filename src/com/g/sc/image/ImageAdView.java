package com.g.sc.image;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.g.sc.AdLogger;
import com.g.sc.MobileInfoGetter;
import com.l.R;
import com.yyxu.download.utils.BitmapLoader;
import com.yyxu.download.utils.BitmapMemoryHelper;
import com.yyxu.download.utils.BitmapUtil;
import com.yyxu.download.utils.MyIntents;
import com.yyxu.download.utils.StorageUtils;

public class ImageAdView extends RelativeLayout implements OnClickListener {

	private ImageAdItem item;
	private MyReceiver receiver;
	private BaseDialog container_dialog;
	
	private RelativeLayout loading_layout;
	private TextView preload_text;
	private ImageView ad_image, close;
	private MyProgress progress_bar;
	private ImageView tiyan;
	
	public ImageAdView(Context context) {
		super(context);
		initParam();
	}

	public ImageAdView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initParam();
	}

	public ImageAdView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initParam();
	}

	private void initParam() {
		View view = inflate(getContext(), R.layout.image_ad_view, null);
		preload_text = (TextView) view.findViewById(R.id.image_ad_preload_text);
		ad_image = (ImageView) view.findViewById(R.id.image_ad_image);
		MsgProgressBar mpb = (MsgProgressBar) view.findViewById(R.id.iamge_ad_loading_bar);
		progress_bar = mpb.getProgress_bar();
		progress_bar.setEnableProgressText(false);
		mpb.getMsg_text_view().setText("正在加载图片");
		loading_layout = (RelativeLayout) view.findViewById(R.id.image_ad_loading_layout);
		tiyan = (ImageView)view.findViewById(R.id.image_ad_tiyan);
		tiyan.setOnClickListener(this);
		close = (ImageView) view.findViewById(R.id.image_ad_close);
		close.setOnClickListener(this);
		addView(view);
		receiver = new MyReceiver();
		BitmapMemoryHelper.context = getContext();
		ad_image.setOnClickListener(this);
	}

	public void setContainer_dialog(BaseDialog container_dialog) {
		this.container_dialog = container_dialog;
	}

	public void init(ImageAdItem item) {
		this.item = item;
		//item.pic_url = "http://www.hfda";
		//item.ad_text = "fdsafhewqkryheiwfnhewqkroewqhfoqwefdsafhewqkryheiwfnhewqkroewqhfoqwefdsafhewqkryheiwfnhewqkroewqhfoqwefdsafhewqkryheiwfnhewqkroewqhfoqwe";
		preload_text.setText(item.ad_text);
		Bitmap bitmap = getItemBitmap();
		if (null != bitmap) {
			hasImage(bitmap);
		} else {
			AdLogger.i("ImageAdView", "DownloadSession download pic");
			DownloadSession.getInstance().downloadPic(getContext(), item);
		}
		autoDownload();
	}

	private void onImageDownloadComplete() {
		Bitmap bitmap = getItemBitmap();
		if (null != bitmap) {
			hasImage(bitmap);
		}
		else
			;
	}
	
	private void hasImage(Bitmap bitmap) {
		loading_layout.setVisibility(View.GONE);
		ad_image.setVisibility(View.VISIBLE);
		ad_image.setImageBitmap(bitmap);
	}

	private Bitmap getItemBitmap() {
		File f = new File(item.getPicPath());
		if (!f.exists())
			return null;
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) getContext()).getWindowManager().getDefaultDisplay()
				.getMetrics(dm);
		int height = dm.heightPixels;
		int width = dm.widthPixels;
		float height_scale = (float) height / 800.f;
		float width_scale = (float) width / 480.f;
		float scale = height_scale < width_scale ? height_scale : width_scale;
		Bitmap bitmap = BitmapLoader.load(item.getPicPath());
		if (null != bitmap) {
		//	bitmap = BitmapUtil.scale(bitmap, scale, scale);
			bitmap = BitmapUtil.getRoundedCornerBitmap(bitmap, 21.5f * (float)(scale >= 1.5 ? scale - 0.4 : scale < 0.8 ? 0.8 : scale));
		}
		return bitmap;
	}

	@Override
	protected void onAttachedToWindow() {
		AdLogger.i("ImageAdView","onAttachedToWindow");
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.yyxu.download.progress");
		getContext().registerReceiver(receiver, filter);
		super.onAttachedToWindow();
	}

	@Override
	protected void onDetachedFromWindow() {
		AdLogger.i("ImageAdView","onDetachedFromWindow");
		getContext().unregisterReceiver(receiver);
			BitmapDrawable dr = (BitmapDrawable) ad_image.getDrawable();
			if (null != dr) {
				dr.getBitmap().recycle();
			}
		super.onDetachedFromWindow();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.image_ad_close) {
			if (container_dialog != null) {
				dismiss();
			}
			return;
		}
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
		dismiss();
	}
	
	private void autoDownload() {
		MobileInfoGetter mig = new MobileInfoGetter(getContext());
		String net_type = mig.getNettype();
		//区分不了当前网络，不直接下载
		if (null == net_type)
			return;
		//只允许wifi状态下直接下载，但是此时的网络是手机网络，不直接下载
		//if (item.net.equalsIgnoreCase("wifi") && !net_type.equalsIgnoreCase("wifi"))
		//	return;
		//其他情况，1：只允许wifi下载，手机网络是wifi 2：允许所有网络下载
		if ("apk".equals(item.link_type)) {
			File f = new File(item.getLinkApkPath());
			if (f.exists()) {
			//	StorageUtils.installAPK(getContext(), f.getPath());
			} else {
				DownloadSession.getInstance().downloadApk(getContext(), item, true);
			}
		}
	}

	private void dismiss() {
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
				AdLogger.i("ImageAdView","type=>" + type + "\nurl=>" + url);
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
					AdLogger.i("ImageAdView","on progress " + progress);
					progress_bar.setProgress(Integer.valueOf(progress));
					break;
				case MyIntents.Types.ERROR:
					Object err = intent.getSerializableExtra(MyIntents.ERROR_CODE);
					AdLogger.i("ImageAdView","加载图片失败 " + (null == err ? "null error" : err.toString()));
					if (null != err && err instanceof IOException) {
						Toast.makeText(getContext(), "加载图片失败", Toast.LENGTH_LONG).show();
						dismiss();
					}
					break;
				default:
					break;
				}
			}
		}
	}

}
