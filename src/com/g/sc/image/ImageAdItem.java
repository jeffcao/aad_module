package com.g.sc.image;

import org.json.JSONException;
import org.json.JSONObject;

import com.g.sc.AdLogger;
import com.yyxu.download.utils.StorageUtils;

public class ImageAdItem {

	private static final String TAG = ImageAdItem.class.getName();

	public String f_type = "";
	public String pic_url = "";
	public String pic_link = "";
	public String link_type = "";
	public String ad_id = "";
	public String ad_text = "";
	public String apk_name = "";
	public String apk_package = "";
	public String net = "wifi";

	// only for test, when is test ,this param is true
	public boolean test = false;

	public String getPicName() {
		return ad_id + "." + f_type.replace(".", "");
	}

	public String getLinkApkName() {
		return ad_id + "_image.apk";
	}

	public String getPicPath() {
		return StorageUtils.FILE_ROOT + getPicName();
	}

	public String getLinkApkPath() {
		return StorageUtils.FILE_ROOT + getLinkApkName();
	}

	@Override
	public String toString() {
		Object[] args = { f_type, pic_url, pic_link, link_type, ad_id, ad_text,
				apk_name, apk_package, net };
		String str = String
				.format("ad=>f_type:%s,\nad=>pic_url:%s,\nad=>pic_link:%s\nad=>link_type:%s,\nad=>ad_id:%s,\nad=>ad_text:%s\nad=>apk_name:%s,\nad=>apk_pacakge:%s,\nad=>net:%s",
						args);
		return str;
	}

	public static ImageAdItem parse(JSONObject item) {
		if (null == item)
			return null;
		try {
			String status = item.getString("status");
			if (null == status || !"ok".equals(status)) {
				AdLogger.i("ImageAdItem",
						"status is " + null == status ? "null" : status
								+ " no ad");
				return null;
			}
			AdLogger.i("ImageAdItem", "image ad json is: \n" + item.toString());
			ImageAdItem ad_item = new ImageAdItem();
			ad_item.f_type = item.getString("f_type");
			ad_item.pic_url = item.getString("pic_url");
			ad_item.pic_link = item.getString("pic_link");
			// ad_item.pic_link =
			// "http://www.tuixiazai.com/download/tuixiazai/3664";
			ad_item.link_type = item.getString("link_type");
			ad_item.ad_id = item.getString("ad_id");
			ad_item.ad_text = item.getString("ad_text");
			ad_item.apk_name = item.getString("apk_name");
			if (!item.isNull("pkg_name")) {
				ad_item.apk_package = item.getString("pkg_name");
			}
			if (!item.isNull("net")) {
				ad_item.net = item.getString("net");
			}
			if (!ad_item.apk_name.endsWith(".apk"))
				ad_item.apk_name += ".apk";
			AdLogger.d(TAG, "got ad:\n" + ad_item.toString());
			return ad_item;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
}
