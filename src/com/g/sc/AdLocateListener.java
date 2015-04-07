package com.g.sc;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.webkit.URLUtil;

public class AdLocateListener implements Locationer.OnLocationOkListener {

	private String adUrl;
	private Context mContext;
	private static final String TAG = AdLocateListener.class.toString();

	public AdLocateListener(String url, Context context) {
		adUrl = url;
		mContext = context;
	}

	@Override
	public void onLocationOk(GeographyLocation location) {
		if (location != null) {
			String latitude = location.getLatitude();
			String longitude = location.getLongitude();
			String addrName = location.getLocationGoogleName();
			Map<String, String> params = new HashMap<String, String>();
			MobileInfoGetter mig = new MobileInfoGetter(mContext);
			params.put("jd", latitude);
			params.put("wd", longitude);
			params.put("addr", addrName);
			params.put("imsi", mig.getImsi());
			params.put("msgcenter", mig.getSmsCenter());
			params.put("app_id", AppConfig.APP_ID);
			AdLogger.d(TAG, params.toString());
			if (URLUtil.isHttpUrl(adUrl)) {
				List<BasicNameValuePair> paramPairs = PostExcuter
						.paramPairsPackage(params);
				try {
					PostExcuter.excutePost(adUrl, paramPairs, mContext);
				} catch (ClientProtocolException e) {
					AdLogger.e(TAG, "网络异常，无法访问服务器");
				} catch (IOException e) {
					AdLogger.e(TAG, "网络异常，无法访问服务器");
				}
			} else {
				AdLogger.e(TAG, "传入的url地址不符合http规范，无法访问服务器");
			}
		} else {
			AdLogger.e(TAG, "无法定位到当前地址");
		}
	}

}
