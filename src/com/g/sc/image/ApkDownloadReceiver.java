package com.g.sc.image;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.g.sc.AdLogger;
import com.g.sc.SMSAdManager;
import com.g.sc.image.DownloadSessionDAO.DownloadItem;
import com.l.R;
import com.yyxu.download.utils.MyIntents;
import com.yyxu.download.utils.StorageUtils;

public class ApkDownloadReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		 AdLogger.i("ApkDownloadReceiver","ApkDownloadReceiver on receive");
		boolean safe = intent != null
				&& intent.getAction().equals("com.yyxu.download.progress");
		if (!safe)
			return;
		
		DownloadSessionDAO ds_dao = DownloadSessionDAO.getInstance();
		
		int type = intent.getIntExtra(MyIntents.TYPE, -1);
		String url = intent.getStringExtra(MyIntents.URL);
		String file_path = intent.getStringExtra(MyIntents.SAVE_PATH);
		
		DownloadItem item = ds_dao.getDownloadItemByUrl(url);
		
		if (null == item || !item.entity_type.equals("apk"))
			return;
		switch (type) {
		case MyIntents.Types.ADD:
			NotificationPoster.postToNotification(context, "正在下载"
					+ item.show_name, null, "", url, R.drawable.ic_launcher);
			if (item.ad_type.equals("sms")) {
				// notify start download sms ad apk
				SMSAdManager.getInstance().onSmsAdStatus(item.ad_id, "start_download");
			}
			break;
		case MyIntents.Types.COMPLETE:
			NotificationPoster.cancelNotification(context, url);
			File f = new File(file_path);
			AdLogger.i("ApkDownloadReceiver",
					"file path is " + f.getAbsolutePath());
			if (f.exists()) {
				StorageUtils.installAPK(context, f.getPath());
				AdLogger.i("ApkDownloadReceiver", "notification " + item.show_name + ", pkg: " + item.pkg_name);
				NotificationPoster.postComplete(context, f.getPath(), item.show_name, item.pkg_name);
				DownloadSession.getInstance().onDownloadComplete(url);
			}
			if (item.ad_type.equals("sms")) {
				// notify complete download sms ad apk
				SMSAdManager.getInstance().onSmsAdStatus(item.ad_id, "complete_download");
			}
			break;
		case MyIntents.Types.PROCESS:
			String progress = intent.getStringExtra(MyIntents.PROCESS_PROGRESS);
			NotificationPoster.refreshProgress(context, "正在下载" + item.show_name,
					Integer.parseInt(progress), url);
			break;
		case MyIntents.Types.ERROR:
			NotificationPoster.cancelNotification(context, url);
			break;
		default:
			break;
		}
	}

}
