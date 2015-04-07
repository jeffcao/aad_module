package com.g.sc.image;

import java.util.ArrayList;
import java.util.List;

public class ImageTest {
	/**
	 * AndroidManifest.xml加入以下
		<service android:name="com.yyxu.download.services.DownloadService" 
            android:exported="false">
            <intent-filter >
                <action android:name="com.yyxu.download.services.IDownloadService" />
            </intent-filter>
        </service>
        <receiver android:name="com.tblin.ad.image.ApkDownloadReceiver"
            android:exported="false">
            <intent-filter >
                <action android:name="com.yyxu.download.progress"/>
            </intent-filter>
        </receiver>
        
        <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
     	<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    	<uses-permission android:name="android.permission.MOUNT_UNMOUNT_FILESYSTEMS" />
    	<uses-permission android:name="android.permission.INTERNET" />
        
        在需要加广告的布局文件里面添加
        com.tblin.ad.image.ImageAdButton
	*/
	public static String[] type = {"apk", "apk", "apk", "apk", "apk", "apk"};
	public static String[] url = {"http://www.nduoa.com/apk/download/587446?from=ndoo",
			"http://www.nduoa.com/apk/download/554844?from=ndoo",
			"http://www.nduoa.com/apk/download/590368?from=ndoo",
			"http://www.nduoa.com/apk/download/581665?from=ndoo",
			"http://www.nduoa.com/apk/download/579037?from=ndoo",
			"http://www.nduoa.com/apk/download/582879?from=ndoo"
			};
	public static String[] pic_url = {"http://attachments.gfan.com/attachments2/day_110308/1103080201cd803b0c7c827531.jpg",
			"http://att.x2.hiapk.com/forum/month_1008/1008121320c83baffd2a15d9a2.jpg",
			"http://img.d.cn/android/android_atta/month_1012/1012231618368e4d5b7b9dcdfa.jpg",
			"http://att.x2.hiapk.com/forum/month_1008/10081213200dd4b7c1024032f9.jpg",
			"http://attachments.gfan.com/attachments2/day_110512/1105121420bcb81c2a6acd9b46.jpg",
			"http://attachments.gfan.com/attachments2/day_110123/11012317089d0482b946b3b2b9.jpg"
			};
	public static String[] apk_packages = {"com.wzlottery", "com.browser2345", "com.storm.smart", "cmccwm.mobilemusic", "com.UCMobile", "com.baidu.browser.apps"};
	public static List<ImageAdItem> items;
	public static String[] nets = {"wifi", "2g3g",  "wifi", "2g3g", "wifi", "2g3g"};
	static{
	//	nets = new String[] {"wifi", "wifi",  "wifi", "wifi", "wifi", "wifi"};
	}
	
	public static List<ImageAdItem> getItems() {
		if (null == items) {
			items = new ArrayList<ImageAdItem>();
			for (int i = 0; i < type.length; i++) {
				ImageAdItem item = new ImageAdItem();
				item.ad_id = (1000 + i) + "";
				item.test = true;
				item.ad_text = "ad text : " + item.ad_id;
				item.link_type = type[i];
				item.f_type = "png";
				item.pic_link = url[i];
				item.pic_url = pic_url[i];
				item.net = nets[i];
				item.apk_package = apk_packages[i];
				if (item.link_type.equals("apk"))
					item.apk_name = nets[i] + (100+i) + ".apk";
				items.add(item);
			}
		}
		return items;
	}
}
