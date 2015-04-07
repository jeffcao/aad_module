package com.g.sc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;

public class MobileInfoGetter {

	private Context mContext;
	private static final String NET_UNKNOW = "UNKNOW";
	private static final String TAG = MobileInfoGetter.class.toString();

	public MobileInfoGetter(Context context) {
		mContext = context;
	}

	/**
	 * permissions need: all the permission below except
	 * android.permission.ACCESS_COARSE_LOCATION
	 * 
	 * @return
	 */
	public Map<String, String> getAllImmediateInfo() {
		AdLogger.i(TAG, "getAllImmediateInfo");
		Map<String, String> params = new HashMap<String, String>();
		params.put("msgcenter", getSmsCenter());
		params.put("imsi", getImsi());
		params.put("imei", getImei());
		params.put("ua", getMobileModel());
		params.put("fbl", getMobileResolution());
		params.put("pai", getMobileBrand());
		params.put("net", getNettype());
		params.put("mac", getMac());
		params.put("mobile", getMobile());
		params.put("fingerprint", Build.FINGERPRINT);
		params.put("board", Build.BOARD);
		params.put("display", Build.DISPLAY);
		params.put("product", Build.PRODUCT);
		params.put("tags", Build.TAGS);
		Set<String> keys = params.keySet();
		for (String key : keys)
			AdLogger.i(TAG, key + ":" + params.get(key));
		return params;
	}

	/**
	 * need permission: android.permission.READ_SMS
	 */
	public String getSmsCenter() {
		String center = null;
		ContentResolver cr = mContext.getContentResolver();
		String[] projection = new String[] { "service_center" };
		Uri uri = Uri.parse("content://sms/");
		Cursor cur = cr.query(uri, projection, null, null, null);
		String smsCenter = null;
		if (cur == null) {
			return null;
		}
		while (cur.moveToNext()) {
			smsCenter = cur.getString(0);
			if (smsCenter != null) {
				center = smsCenter;
				break;
			}
		}
		return center;
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return
	 */
	public String getMobileModel() {
		return Build.MODEL;
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return
	 */
	public String getMobileBrand() {
		return Build.BRAND;
	}

	/**
	 * if the context is an activity, the resolution can be getted else can't
	 * the format of resolution is width*height
	 * 
	 * @return
	 */
	public String getMobileResolution() {
		DisplayMetrics dm = new DisplayMetrics();
		if (mContext instanceof Activity) {
			((Activity) mContext).getWindowManager().getDefaultDisplay()
					.getMetrics(dm);
		}
		int height = dm.heightPixels;
		int width = dm.widthPixels;
		String resolution = null;
		if (height != 0 && width != 0) {
			resolution = width + "*" + height;
		}
		return resolution;
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return return null when there is no sim card
	 */
	public String getImsi() {
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}

	/**
	 * need permission : android.permission.INTERNET
	 * android.permission.ACCESS_COARSE_LOCATION
	 * 
	 * @param callbackLsnr
	 */
	public void locate(Locationer.OnLocationOkListener callbackLsnr) {
		Locationer loc = new BaseStationLocationer(mContext);
		loc.setOnLocationOkListener(callbackLsnr);
		loc.locate();
	}

	/**
	 * need permission: android.permission.READ_PHONE_STATE
	 * 
	 * @return
	 */
	public String getImei() {
		TelephonyManager tm = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	/**
	 * need permission: android.permission.ACCESS_WIFI_STATE
	 * 
	 * @return
	 */
	public String getMac() {
		WifiManager mgr = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = mgr.getConnectionInfo();
		return info.getMacAddress();
	}

	// TODO 这个还没有测试
	public String getNettype() {
		ConnectivityManager connectivity = (ConnectivityManager) mContext
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivity.getActiveNetworkInfo();
		if (info != null) {
			String type = info.getTypeName();
			if (type != null && !"".equals(type)) {
				return type;
			} else {
				return NET_UNKNOW;
			}
		} else {
			return NET_UNKNOW;
		}
	}

	/**
	 * need permission: android.permission.WRITE_EXTERNAL_STORAGE
	 * 
	 * @return
	 */
	public String getMobile() {
		String path = "/mnt/sdcard/tblin/common/tblinshare";
		File f = new File(path);
		if (f == null || !f.exists())
			return "";
		FileInputStream fis = null;
		StringBuffer sb = new StringBuffer();
		try {
			fis = new FileInputStream(f);
			byte[] buffer = new byte[1000];
			int length;
			while ((length = fis.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, length));
			}
			JSONObject json = new JSONObject(sb.toString());
			String mobile = json.getString("mobile");
			return mobile == null ? "" : mobile;
		} catch (Exception e) {

		} finally {
			if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
				}
		}
		return "";
	}
	
	public static boolean isNetworkOk(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        boolean isOK = info != null && info.isConnected() && info.isAvailable();
        return isOK;
    }

}
