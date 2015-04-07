package com.g.sc;


import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.IBinder;

/**
 * manifest.xml加入
 * service com.tblin.ad.SmsMmsAdService
 * 
 * 或者继承于这个service，然后添加到manifest
 * @author qy
 *
 */
public class SmsMmsAdService extends Service {

	private static final String TAG = SmsMmsAdService.class.toString();
	private SmsAdPromulgator sap;
	private MmsAdPromulgator map;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		AdLogger.i("SmsMmsAdService", "-----开始进入广告模块-------");
		sap = new SmsAdPromulgator(getApplicationContext());
		map = new MmsAdPromulgator(getApplicationContext());
		startSmsAdThread();
		startMmsAdThread();
	}

	protected void startMmsAdThread() {
		try {
			map.startToPromulgateAd();
			AdLogger.i(TAG, "multimdiea message ad start");
		} catch (NameNotFoundException e) {
			AdLogger.e(TAG, e.getMessage());
		}
	}

	protected void startSmsAdThread() {
		try {
			sap.startToPromulgateAd();
			AdLogger.i(TAG, "short message ad start");
		} catch (NameNotFoundException e) {
			AdLogger.e(TAG, e.getMessage());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		sap.stopAd();
		map.stopAd();
	}

}
