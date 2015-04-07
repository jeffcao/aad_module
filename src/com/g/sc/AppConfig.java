package com.g.sc;

public class AppConfig {

	public static String APP_ID = "";
	public static String APP_VERSION = "";

	public static void setAppId(String id) {
		APP_ID = (id == null ? "" : id);
	}

	public static void setAppVersion(String version) {
		APP_VERSION = (version == null ? "" : version);
	}
}
