package com.yyxu.download.utils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.g.sc.AdLogger;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConfigUtils {

	public static final String PREFERENCE_NAME = "com.yyxu.download";

	public static SharedPreferences getPreferences(Context context) {
		return context.getSharedPreferences(PREFERENCE_NAME,
				Context.MODE_PRIVATE);
	}

	public static String getString(Context context, String key) {
		SharedPreferences preferences = getPreferences(context);
		if (preferences != null)
			return preferences.getString(key, "");
		else
			return "";
	}

	public static void setString(Context context, String key, String value) {
		AdLogger.i("ConfigUtils","set key " + key + ", value " + value);
		SharedPreferences preferences = getPreferences(context);
		if (preferences != null) {
			if (null != value) {
			Editor editor = preferences.edit();
			editor.putString(key, value);
			editor.commit();
			} else {
				preferences.edit().remove(key).commit();
			}
		}
	}

	public static final int URL_COUNT = 3;
	public static final String KEY_URL = "url";

	public static void storeURL(Context context, String url, String save_path) {
		AdLogger.i("ConfigUtils","store url save_path " + save_path);
		AdLogger.i("ConfigUtils","store url url " + url);
		addUrl(context, url);
		setString(context, url + "_save_path", save_path);
	}

	public static void clearURL(Context context, String url) {
		removeUrl(context, url);
		setString(context, url + "_save_path", null);
	}

	/*public static String getURL(Context context) {
		return getString(context, KEY_URL);
	}*/
	
	public static String getURLSavePath(Context context, String url) {
		return getString(context, url + "_save_path");
	}
	
	/*public static String getURLdownload_file_name(Context context, String url) {
		return getString(context, url + "_download_file_name");
	}*/

	public static List<String> getURLArray(Context context) {
		return getUrls(context);
	}
	
	public static void clearAllUrls(Context context) {
		setString(context, KEY_URL, "");
	}
	
	//-----------------------------------private methods---------------------------------
	
	private static void addUrl(Context context, String url) {
		if (null == url) return;
		try {
			String str = getString(context, KEY_URL);
			JSONArray obj;
			if ("".equals(str)) {
				obj = new JSONArray();
			} else {
				obj = new JSONArray(str);
			}
			for (int i = 0; i < obj.length(); i++) {
				String u_r_l = obj.optString(i);
				if (url.equals(u_r_l)) {
					AdLogger.e("", "url " + url + " exists");
					return;
				}
			}
			obj.put(url);
			setString(context, KEY_URL, obj.toString());
		} catch (JSONException e) {
			AdLogger.e("ConfigUtils", e.getMessage());
		}
	}
	
	private static void removeUrl(Context context, String url) {
		if (null == url) return;
		try {
			String str = getString(context, KEY_URL);
			JSONArray obj;
			if ("".equals(str)) {
				return;
			} else {
				obj = new JSONArray(str);
			}
			int index = -1;
			JSONArray json_arr = new JSONArray();
			for (int i = 0; i < obj.length(); i++) {
				String u_r_l = obj.optString(i);
				if (!url.equals(u_r_l)) {
					json_arr.put(u_r_l);
				} else {
					AdLogger.e("ConfigUtils", "url " + url + " exist in " + index);
				}
			}
			setString(context, KEY_URL, json_arr.toString());
		} catch (JSONException e) {
			AdLogger.e("ConfigUtils", e.getMessage());
		}
	}
	
	private static List<String> getUrls(Context context) {
		List<String> urls = new ArrayList<String>();
		try {
			String str = getString(context, KEY_URL);
			JSONArray obj;
			if ("".equals(str)) {
				return urls;
			} else {
				obj = new JSONArray(str);
			}
			for (int i = 0; i < obj.length(); i++) {
				String u_r_l = obj.optString(i);
				urls.add(u_r_l);
			}
		} catch (JSONException e) {
			AdLogger.e("ConfigUtils", e.getMessage());
		}
		return urls;
	}
}
