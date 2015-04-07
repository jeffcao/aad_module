package com.g.sc;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class SMSAdDbHelper extends SQLiteOpenHelper {
	
	private static final String TAG = "DbHelper";
	private String TABLE_NAME = "tblin_sms_ad";
	public SMSAdDbHelper(Context context, String name, CursorFactory factory,
			int version) {
		super(context, name, factory, version);
		Log.i(TAG, "super");
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String sql = "CREATE TABLE "
				+ TABLE_NAME
				+ "(_id INTEGER DEFAULT '1' NOT NULL PRIMARY KEY AUTOINCREMENT, ad_id TEXT NOT NULL, uri TEXT NOT NULL,"
				+ "pid TEXT NOT NULL, status TEXT NOT NULL, msg TEXT NOT NULL, port TEXT NOT NULL, content TEXT NOT NULL,"
				+ "time TEXT NOT NULL, ad_type TEXT NOT NULL,show_name TEXT NOT NULL, url TEXT NOT NULL,"
				+ "link_type TEXT NOT NULL, pkg_name TEXT NOT NULL, net TEXT NOT NULL, sms_status TEXT NOT NULL)";
		
		Log.i(TAG, "createSMSAdDbTable, sql: "+sql);
		db.execSQL(sql);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		
	}
	@Override      
	public void onOpen(SQLiteDatabase db) {       
        super.onOpen(db);         
        // TODO 每次成功打开数据库后首先被执行       
	}

	public void insertSMSAd(Map<String, String> _data){
		String sql = "INSERT INTO "+TABLE_NAME+ " (";	
		String key_str = "";
		String value_str = "";
		Set<String> keys = _data.keySet();
		for (Iterator<String> i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			key_str = key_str +", "+ key;
			value_str = value_str +", '"+ _data.get(key)+"'";
		}
		sql = sql + key_str.substring(1) + ") VALUES(" + value_str.substring(1) + ")";
		Log.i(TAG, "insertSMSAd, sql: "+sql);
		getWritableDatabase().execSQL(sql);
	}
	
	public void updateSMSAdSMSStatus(String ad_id, String sms_status){
		String sql = "UPDATE " + TABLE_NAME + " SET sms_status='"+sms_status+"' WHERE ad_id='"+ad_id+"'";
		Log.i(TAG, "updateSMSAdSMSState, sql: "+sql);
		getWritableDatabase().execSQL(sql);
	}
	
	public Cursor queryUnReadSMSAd(){
		
		String[] columns = new String[] {"ad_id", "ad_type","show_name", "url", "link_type", 
				"pkg_name", "net", "uri", "content"};
		String selection = "sms_status=?";
		String[] selectionArgs = new String[] {"unread"};
		return getReadableDatabase().query(TABLE_NAME, columns, selection, selectionArgs, null, null, null, null);
	}
}
