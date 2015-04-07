package com.g.sc.image;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.g.sc.AdLogger;
import com.g.sc.AdManager;
import com.g.sc.AppConfig;
import com.g.sc.MobileInfoGetter;
import com.g.sc.PostExcuter;
import com.l.R;

public class ImageAdButton extends ImageView implements View.OnClickListener {
	private AnimationDrawable anim;
	private ProgressDialog requestAdProgress;
	private static final String TAG = ImageAdButton.class.getName();
	private static final String ImageAdUrl = "http://wap.cn6000.com/cm/andr/send_ad_tc.php";

	public ImageAdButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public ImageAdButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public ImageAdButton(Context context) {
		super(context);
		init();
	}

	private void init() {
		this.setOnClickListener(this);
		DragView lsnr = new DragView();
		lsnr.init(getContext());
		this.setOnTouchListener(lsnr);
	}

	public void setAd_id(String ad_id) {
		AppConfig.setAppId(ad_id);
	}

	public void setVersion(String version) {
		AppConfig.setAppVersion(version);
	}

	@Override
	protected void onAttachedToWindow() {
		this.setBackgroundResource(R.drawable.press_me);
		this.anim = (AnimationDrawable) this.getBackground();
		this.anim.start();
		super.onAttachedToWindow();
	}

	@Override
	public void onClick(View v) {
		boolean has_network = MobileInfoGetter.isNetworkOk(getContext());
		if (!has_network) {
			Toast.makeText(getContext(), "没有网络连接", Toast.LENGTH_LONG).show();
			return;
		}
		requestAdProgress = new ProgressDialog(getContext());
		requestAdProgress.setEnableProgressText(false);
		requestAdProgress.setMsg("正在加载广告");
		requestAdProgress.show();
		requestAd();
	}

	private void requestAd() {
		Runnable r = new Runnable() {

			@Override
			public void run() {
				ImageAdItem item = null;
				try {
					String json = getJson();
					if (null != json) {
						JSONObject obj = new JSONObject(json);
						item = ImageAdItem.parse(obj);
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				} finally {
				//	List<ImageAdItem> items = ImageTest.getItems();
				//	item = items.get((int) (Math.random() * (float) items
				//	 .size()));
					onAdRequested(item);
				}
			}
		};
		new Thread(r).start();
	}

	private void onAdRequested(final ImageAdItem item) {
		post(new Runnable() {

			@Override
			public void run() {
				if (null != requestAdProgress && requestAdProgress.isShowing()) {
					requestAdProgress.dismiss();
				}
				if (null != item) {
					ImageAdView ia = new ImageAdView(getContext());
					/*
					 * LayoutParams param = new LayoutParams(
					 * LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
					 * param.addRule(RelativeLayout.CENTER_HORIZONTAL);
					 */
					ia.init(item);
					// Builder builder = new Builder(getContext());
					// builder.setView(ia);
					BaseDialog dialog = new BaseDialog(getContext());
					dialog.setContentView(ia);
					ia.setContainer_dialog(dialog);
					dialog.show();
				} else {
					Toast.makeText(getContext(), "加载失败", Toast.LENGTH_LONG)
							.show();
				}
			}
		});
	}

	/**
	 * 当网络出现异常时会抛出异常 当网络正常而访问的url不正常时，返回值为空 一切正常时，返回相应的字符串
	 * 
	 * @return
	 * @throws IOException
	 * @throws ClientProtocolException
	 * @throws Exception
	 */
	private String getJson() throws IOException {
		MobileInfoGetter mig = new MobileInfoGetter(getContext());
		Map<String, String> params = mig.getAllImmediateInfo();
		AdLogger.i(TAG, "sms app id is: <" + AppConfig.APP_ID + ">");
		AdLogger.i(TAG, "sms app version is: <" + AppConfig.APP_VERSION + ">");
		params.put("app_id", AppConfig.APP_ID);
		params.put("version", AppConfig.APP_VERSION);
		params.put("a_version", AdManager.getInstance().getVersion());
		List<BasicNameValuePair> pairs = PostExcuter.paramPairsPackage(params);
		return PostExcuter.excutePost(ImageAdUrl, pairs, getContext());
	}

}
