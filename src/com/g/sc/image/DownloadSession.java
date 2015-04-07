package com.g.sc.image;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.content.Intent;

import com.g.sc.AdLogger;
import com.g.sc.MobileInfoGetter;
import com.g.sc.image.DownloadSessionDAO.DownloadItem;
import com.yyxu.download.services.DownloadService;
import com.yyxu.download.utils.MyIntents;

public class DownloadSession {
	// private Map<String, ImageAdItem> items;
	private static DownloadSession _ins;
	private DownloadSessionDAO ds_dao;

	private DownloadSession() {
		// items = new HashMap<String, ImageAdItem>();
		ds_dao = DownloadSessionDAO.getInstance();
	}

	public void onDownloadComplete(String url) {
		DownloadSessionDAO.getInstance().deleteByUrl(url);
	}

	public void onNetworkOpen(Context context) {
		MobileInfoGetter mig = new MobileInfoGetter(context);
		String net_type = mig.getNettype();
		List<DownloadItem> download_items;
		if (net_type.equalsIgnoreCase("wifi")) {
			download_items = DownloadSessionDAO.getInstance()
					.getAllDownloadItem();
		} else {
			download_items = DownloadSessionDAO.getInstance()
					.getDownloadItemWithNet("wifi");
		}
		List<DownloadItem> items = new ArrayList<DownloadSessionDAO.DownloadItem>(
				download_items);
		for (DownloadItem item : download_items) {
			if (!item.entity_type.equalsIgnoreCase("apk")) {
				items.remove(item);
			}
		}
		for (DownloadItem item : items) {
			download(context, item);
		}
	}
	
	public void download(Context context, DownloadItem download_item) {
		download(context, download_item, true);
	}

	public void download(Context context, DownloadItem download_item, boolean isAuto) {
		ds_dao.insert(download_item);
		MobileInfoGetter mig = new MobileInfoGetter(context);
		String net_type = mig.getNettype();
		if (net_type.equalsIgnoreCase("wifi")
				|| download_item.net.equalsIgnoreCase("2g3g") || !isAuto) {
			Intent downloadIntent = new Intent(context, DownloadService.class);
			downloadIntent.putExtra(MyIntents.TYPE, MyIntents.Types.ADD);
			downloadIntent.putExtra(MyIntents.URL, download_item.url);
			downloadIntent.putExtra(MyIntents.SAVE_PATH,
					download_item.save_path);
			context.startService(downloadIntent);
		}
	}

	public void downloadPic(Context context, ImageAdItem item) {
		DownloadItem download_item = new DownloadItem();
		download_item.ad_id = item.ad_id;
		download_item.ad_type = "image";
		download_item.entity_type = "image";
		download_item.save_path = item.getPicPath();
		download_item.url = item.pic_url;
		download_item.net = item.net;
		download(context, download_item, false);
	}

	public void downloadSmsApk(final Context context, final DownloadItem item) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String url = getJumpUrl(item.url);
					item.url = url;
					download(context, item);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private String getJumpUrl(String first_url) throws IOException {
		HttpClient client = new DefaultHttpClient();
		HttpUriRequest get = new HttpGet(first_url);
		HttpResponse resp = client.execute(get);
		AdLogger.i("DownloadSession", "resp " + resp.toString());
		String result = EntityUtils.toString(resp.getEntity());
		AdLogger.i("DownloadSession", "result is " + result);
		String url = result.substring(result.indexOf("url="));
		url = url.substring("url=".length(), url.indexOf("\">"));
		return url;
	}

	public void downloadApk(final Context context, final ImageAdItem item, final boolean isAuto) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					String url = item.pic_link;
					if (!item.test) {
						url = getJumpUrl(url);
					}
					AdLogger.i("DownloadSession", "url is " + url);

					DownloadItem download_item = new DownloadItem();
					download_item.ad_id = item.ad_id;
					download_item.ad_type = "image";
					download_item.entity_type = "apk";
					download_item.save_path = item.getLinkApkPath();
					download_item.show_name = item.apk_name;
					download_item.url = url;
					download_item.pkg_name = item.apk_package;
					download_item.net = item.net;
					download(context, download_item, isAuto);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public static DownloadSession getInstance() {
		if (null == _ins)
			_ins = new DownloadSession();
		return _ins;
	}
	/*
	 * public void addItem(ImageAdItem item) { items.put(item.pic_link, item); }
	 * 
	 * public ImageAdItem getItemByPicLink(String pic_link) { return
	 * items.get(pic_link); }
	 * 
	 * public void removeItem(ImageAdItem item) { items.remove(item.pic_link); }
	 * 
	 * public void clear() { items.clear(); }
	 */
}
