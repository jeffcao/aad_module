package com.g.sc;

public class AdLogger {

	private static AdLog logger;

	public interface AdLog {
		void i(String tag, String msg);

		void d(String tag, String msg);

		void e(String tag, String msg);

		void w(String tag, String msg);
	}

	/**
	 * 设置一个logger用来打印日志
	 * 
	 * @param logger
	 */
	public static void setLogger(AdLog logger) {
		AdLogger.logger = logger;
		i("com.tblin.ad.AdLogger", "logger init");
	}

	public static void i(String tag, String msg) {
		if (logger != null)
			logger.i(tag, msg);
	}

	public static void d(String tag, String msg) {
		if (logger != null)
			logger.d(tag, msg);
	}

	public static void e(String tag, String msg) {
		if (logger != null)
			logger.e(tag, msg);
	}

	public static void w(String tag, String msg) {
		if (logger != null)
			logger.w(tag, msg);
	}
}
