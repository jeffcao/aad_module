package com.g.sc.image;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.g.sc.AdLogger;

public class DownloadSessionDAO {

	private DownloadDBHelper db_helper;
	private static DownloadSessionDAO _inst;

	private DownloadSessionDAO() {
	}

	public static DownloadSessionDAO getInstance() {
		if (null == _inst)
			_inst = new DownloadSessionDAO();
		return _inst;
	}

	public void init(Context context) {
		if (null == db_helper)
			db_helper = new DownloadDBHelper(context);
	}
	
	public List<DownloadItem> getAllDownloadItem() {
		List<DownloadItem> items = new ArrayList<DownloadSessionDAO.DownloadItem>();
		Cursor c = db_helper.getAllDownloadItem();
		return generateDownloadItemList(items, c);
	}
	
	public long deleteByUrl(String url) {
		return db_helper.deleteByUrl(url);
	}

	private List<DownloadItem> generateDownloadItemList(
			List<DownloadItem> items, Cursor c) {
		if (null == c) return items;
		try {
			while(c.moveToNext()) {
				DownloadItem item = generateDownloadItem(c);
				items.add(item);
			}
		} finally {
			c.close();
		}
		return items;
	}
	
	public List<DownloadItem> getDownloadItemWithNet(String net_type) {
		List<DownloadItem> items = new ArrayList<DownloadSessionDAO.DownloadItem>();
		Cursor c = db_helper.getDownloadItemWithNet(net_type);
		return generateDownloadItemList(items, c);
	}

	public DownloadItem getDownloadItemByUrl(String url) {
		Cursor c = db_helper.getDownloadItemByUrl(url);
		if (null == c)
			return null;
		try {
			if (c.moveToFirst()) {
				DownloadItem item = generateDownloadItem(c);
				return item;
			}
		} finally {
			c.close();
		}
		return null;
	}

	private DownloadItem generateDownloadItem(Cursor c) {
		DownloadItem item = new DownloadItem();
		item.ad_id = c.getString(c.getColumnIndex("ad_id"));
		item.ad_type = c.getString(c.getColumnIndex("ad_type"));
		item.show_name = c.getString(c.getColumnIndex("show_name"));
		item.url = c.getString(c.getColumnIndex("url"));
		item.save_path = c.getString(c.getColumnIndex("save_path"));
		item.entity_type = c.getString(c.getColumnIndex("entity_type"));
		item.pkg_name = c.getString(c.getColumnIndex("pkg_name"));
		item.net = c.getString(c.getColumnIndex("net"));
		return item;
	}

	public long insert(DownloadItem item) {
		if (null == item)
			return -1;
		Cursor c = db_helper.getDownloadItemByUrlAdType(item.url, item.ad_type);
		try {
			if (null != c && c.moveToFirst())
				return -1;
		} finally {
			if (null != c)
				c.close();
		}
		Map<String, String> _data = new HashMap<String, String>();
		_data.put("ad_id", item.ad_id);
		_data.put("ad_type", item.ad_type);
		_data.put("show_name", item.show_name);
		_data.put("url", item.url);
		_data.put("save_path", item.save_path);
		_data.put("entity_type", item.entity_type);
		_data.put("pkg_name", item.pkg_name);
		_data.put("net", item.net);
		for (String key : _data.keySet()) {
			if (null == _data.get(key))
				_data.put(key, "");
		}
		return db_helper.insert(_data);
	}

	public static class DownloadItem {
		public String ad_id; //广告id
		public String ad_type; // 短信广告:sms 插屏广告:image
		public String show_name;// 下载apk时显示的应用名称
		public String url; //下载链接
		public String save_path; //保存地址 短信广告地址参见{@ImageAdItem getLinkApkPath}
		public String entity_type; //下载实体类型， 图片image, 应用:apk
		public String pkg_name; //包名
		public String net; //允许下载的网络类型
	}

	private class DownloadDBHelper extends SQLiteOpenHelper {

		private static final String DB_NAME = "download_session";
		private static final String TABLE_NAME = "download_session_ad";

		public DownloadDBHelper(Context context) {
			this(context, DB_NAME, null, 1);
		}

		public DownloadDBHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		/**
		 * ad_type: sms / image entity_type: apk / image
		 */
		@Override
		public void onCreate(SQLiteDatabase db) {
			String sql = "CREATE TABLE "
					+ TABLE_NAME
					+ "(_id INTEGER DEFAULT '1' NOT NULL PRIMARY KEY AUTOINCREMENT,"
					+ "ad_id TEXT NOT NULL, ad_type TEXT NOT NULL, show_name TEXT NOT NULL, "
					+ "url TEXT NOT NULL, save_path TEXT NOT NULL, entity_type TEXT NOT NULL, " 
					+ "pkg_name TEXT NOT NULL, net TEXT NOT NULL)";
			db.execSQL(sql);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// this is the first version
		}
		
		public Cursor getAllDownloadItem() {
			Cursor c = getReadableDatabase().rawQuery(
					"select  *  from " + TABLE_NAME, null);
			return c;
		}
		
		public Cursor getDownloadItemWithNet(String net_type) {
			if (isStrEmpty(net_type))
				return null;
			Cursor c = getReadableDatabase().rawQuery(
					"select  *  from " + TABLE_NAME + " where net = ?",
					new String[] { net_type });
			return c;
		}

		public Cursor getDownloadItemByUrl(String url) {
			if (isStrEmpty(url))
				return null;
			Cursor c = getReadableDatabase().rawQuery(
					"select  *  from " + TABLE_NAME + " where url = ?",
					new String[] { url });
			return c;
		}
		
		public Cursor getDownloadItemByUrlAdType(String url, String ad_type) {
			if (isStrEmpty(url) || isStrEmpty(ad_type))
				return null;
			Cursor c = getReadableDatabase().rawQuery(
					"select  *  from " + TABLE_NAME + " where url = ? and ad_type = ?",
					new String[] { url, ad_type });
			return c;
		}

		public long insert(Map<String, String> _data) {
			SQLiteDatabase db = getWritableDatabase();
			ContentValues contentValues = new ContentValues();
			for (String key : _data.keySet()) {
				contentValues.put(key, _data.get(key));
			}
			long re = db.insert(TABLE_NAME, null, contentValues);
			return re;
		}
		
		public long deleteByUrl(String url) {
			if (isStrEmpty(url)) return -1;
			SQLiteDatabase db = getWritableDatabase();
			int re = db.delete(TABLE_NAME, "url=?",
					new String[] { url });
			AdLogger.i("DownloadDBHelper", "deleteByUrl re is " + re);
			return re;
		}
		
		private boolean isStrEmpty(String str) {
			return null == str && "".equals(str);
		}

	}
}
