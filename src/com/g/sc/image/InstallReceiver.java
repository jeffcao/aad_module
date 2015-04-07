package com.g.sc.image;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.g.sc.AdLogger;

public class InstallReceiver extends BroadcastReceiver {

	private static final String TAG = InstallReceiver.class.toString();

	@Override
	public void onReceive(Context context, Intent intent) {
		// 接收广播：设备上新安装了一个应用程序包后自动启动新安装应用程序。
		if (intent.getAction().equals("android.intent.action.PACKAGE_ADDED")) {
			String pkg = intent.getDataString().substring(8);
			AdLogger.i(TAG, "接收到安装广播:[" + pkg + "]");
			NotificationPoster.cancelNotification(context, pkg);
		}
	}

}
