package com.g.sc.image;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.g.sc.AdLogger;
import com.l.R;

public class NotificationPoster {

	public static final String SEVEN_BLANK = "       ";
	public static final String NOTIFICATION_TAG = "notification_tag";
	public static final String TAG = NotificationPoster.class.toString();
	private static final Map<String, Notification> _data = new HashMap<String, Notification>();

	public static void postToNotification(Context mContext, String message,
			Class<?> cls, String title, String tag, int icon) {
		Intent intent = new Intent();  
		Notification notif; 
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);  
        NotificationManager     manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);  
        notif = new Notification();  
        notif.icon = R.drawable.ic_launcher; 
        notif.flags |= Notification.FLAG_NO_CLEAR;
        notif.tickerText = message;  
        //通知栏显示所用到的布局文件  
        notif.contentView = new RemoteViews(mContext.getPackageName(), R.layout.notify);  
        notif.contentIntent = pIntent;  
        notif.contentView.setTextViewText(R.id.content_view_text1, message + SEVEN_BLANK + "0"+"%");  
        notif.contentView.setProgressBar(R.id.content_view_progress, 100, 0, false);
        manager.notify(tag, 0, notif);  
        _data.put(tag, notif);
	}
	
	public static void postComplete(Context mContext, String apk_path, String apk_name, String package_name) {
		String message =  apk_name + "已下载完毕";
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(new File(apk_path)),
				"application/vnd.android.package-archive");
		Notification notif; 
        PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);  
        NotificationManager     manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);  
        notif = new Notification();  
        notif.icon = R.drawable.ic_launcher; 
        notif.flags |= Notification.FLAG_NO_CLEAR;
        notif.tickerText = message;
        notif.setLatestEventInfo(mContext, "点击安装", message,
        		pIntent);
        manager.notify(package_name, 0, notif);  
        _data.put(package_name, notif);
	}
	
	public static void refreshProgress(Context mContext, String msg, int progress, String tag) {
		if (progress < 0 || progress > 100) {
			AdLogger.e("NotificationPoster", "progress error " + progress);
			return;
		}
		Notification notif = _data.get(tag);
		if (null == notif) {
			postToNotification(mContext, msg, null, null, tag, -1);
			notif = _data.get(tag);
		}
		NotificationManager   manager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);  
		notif.contentView.setTextViewText(R.id.content_view_text1, msg + SEVEN_BLANK + progress + "%");  
        notif.contentView.setProgressBar(R.id.content_view_progress, 100, progress, false);
        manager.notify(tag,0, notif);  
	}
	
	/*public static void postToNotification(Context mContext, String message,
			Class<?> cls, String title, String tag, int icon) {
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService("notification");
		Notification notification = new Notification();
		notification.icon = icon;
		notification.tickerText = message;
		notification.contentView = new RemoteViews(mContext.getPackageName(), R.layout.notify);
		notification.contentView.setProgressBar(R.id.content_view_progress, 100, 10, false);
		//notification.defaults = Notification.DEFAULT_SOUND;
		Intent intent = new Intent();
		intent.putExtra(NOTIFICATION_TAG, true);
		//intent.setClass(mContext, cls);
		//notification.contentIntent = PendingIntent.getActivity(mContext, 0, new Intent(mContext, RemoteView.class), 0);
		PendingIntent m_PendingIntent = PendingIntent.getActivity(mContext, 0,
				intent, 0);
		notification.setLatestEventInfo(mContext, title, message,
				m_PendingIntent);
		notification.flags |= Notification.FLAG_NO_CLEAR;
		notificationManager.notify(tag, 0, notification);
	}*/
	
	public static void cancelNotification(Context mContext, String tag) {
		_data.remove(tag);
		NotificationManager notificationManager = (NotificationManager) mContext
				.getSystemService("notification");
		notificationManager.cancel(tag, 0);
	}

}
