package com.g.sc;

import com.g.sc.image.DownloadSessionDAO;

import android.content.Context;

public class AdManager {
	
	private Context mContext;
	private static AdManager _inst;
	private final String version = "1.0";
	
	private AdManager() {}
	
	public static AdManager getInstance() {
		if (null == _inst)
			_inst = new AdManager();
		return _inst;
	}
	
	public void init(Context context, String ad_id, String app_version) {
		if (null == mContext && null != context) {
			mContext = context;
			AppConfig.APP_ID = ad_id;
			AppConfig.APP_VERSION = app_version;
			//do init action in here
			DownloadSessionDAO.getInstance().init(context);
			SMSAdManager.getInstance().init(mContext);
		}
	}
	
	public String getVersion() {
		return version;
	}
}
