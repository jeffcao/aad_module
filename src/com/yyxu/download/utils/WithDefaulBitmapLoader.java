package com.yyxu.download.utils;

import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

import com.g.sc.AdLogger;

/**
 * 不要recycle掉getDefault()产生的Bitmap
 * 在recycle时，要先判断是不是等于WithDefaulBitmapLoader.getDefault()
 * 
 * @author qy
 * 
 */
public class WithDefaulBitmapLoader {

	public static int DEFAULT_BITMAP_RESOURCE_ID = 1;
	private static Bitmap defaultBitmap;
	public static Context context = null;
	public static int MAX_HEIGHT = 1280;
	public static int MAX_WIDTH = 720;
	
	public static Bitmap loadWithSpec(InputStream stream) {
		Bitmap bitmap = BitmapLoader.load(stream, MAX_HEIGHT, MAX_WIDTH);
		return null != bitmap ? bitmap : getDefault();
	}
	
	public static Bitmap loadWithSpec(String filePath) {
		Bitmap bitmap = BitmapLoader.load(filePath, MAX_HEIGHT, MAX_WIDTH);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(InputStream stream) {
		Bitmap bitmap = BitmapLoader.load(stream);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(InputStream stream, final int maxHeight,
			final int maxWidth) {
		Bitmap bitmap = BitmapLoader.load(stream, maxHeight, maxWidth);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(InputStream stream, final int minSampleSize) {
		Bitmap bitmap = BitmapLoader.load(stream, minSampleSize);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(Options opts, final int minSampleSize,
			InputStream stream) {
		Bitmap bitmap = BitmapLoader.load(opts, minSampleSize, stream);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(String filePath) {
		Bitmap bitmap = BitmapLoader.load(filePath);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(String filePath, final int maxHeight,
			final int maxWidth) {
		Bitmap bitmap = BitmapLoader.load(filePath, maxHeight, maxWidth);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(String filePath, final int minSampleSize) {
		Bitmap bitmap = BitmapLoader.load(filePath, minSampleSize);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap load(Options opts, final int minSampleSize,
			String filePath) {
		Bitmap bitmap = BitmapLoader.load(opts, minSampleSize, filePath);
		return null != bitmap ? bitmap : getDefault();
	}

	public static Bitmap getDefault() {
		AdLogger.i("WIthDefaultBitmapLoader", "current default is "
				+ (null == defaultBitmap ? "null" : defaultBitmap
						+ " is recycled: " + defaultBitmap.isRecycled()));
		if (null == defaultBitmap || defaultBitmap.isRecycled()) {
			defaultBitmap = BitmapFactory.decodeResource(
					context.getResources(), DEFAULT_BITMAP_RESOURCE_ID);
		}
		return defaultBitmap;
	}
}
