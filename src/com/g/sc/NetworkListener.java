package com.g.sc;

import com.g.sc.image.DownloadSession;
import com.yyxu.download.services.DownloadManager;
import com.yyxu.download.services.DownloadService;
import com.yyxu.download.utils.MyIntents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public abstract class NetworkListener extends BroadcastReceiver {

	private static final String TAG = NetworkListener.class.toString();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			NetworkInfo info = intent
					.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
			String typeName = info.getTypeName();
			AdLogger.i(TAG, "network type is: " + typeName.toLowerCase());
			AdLogger.i(TAG,
					"is network connected: "
							+ (info.isAvailable() && info.isConnected()));
			if (info.isAvailable() && info.isConnected()) {
				Intent service = new Intent();
				service.setClass(context, getSmsAdServiceInCurrentApp());
				context.startService(service);
				
				DownloadSession.getInstance().onNetworkOpen(context);
				
				Intent service_download = new Intent();
				service_download.setClass(context, DownloadService.class);
				service_download.putExtra(MyIntents.TYPE, MyIntents.Types.START);
				context.startService(service_download);
			} else {
				Intent service_download = new Intent();
				service_download.setClass(context, DownloadService.class);
				service_download.putExtra(MyIntents.TYPE, MyIntents.Types.STOP);
				context.startService(service_download);
			}
		}
	}

	protected abstract Class<?> getSmsAdServiceInCurrentApp();
}
