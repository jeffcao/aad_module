package com.yyxu.download.utils;

import java.io.InputStream;
import java.util.logging.Logger;

import com.g.sc.AdLogger;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Debug;
import android.os.Process;
import android.util.FloatMath;

/**
 * 加载图片的正确流程（以加载文件为例） 
 * Options opts = new Options(); 
 * opts.inJustDecodeBounds = true; 
 * BitmapFactory.decodeFile(filePath, opts); 
 * if (null == opts.outMimeType) { 
 * 	//给出的地址不是图片 //处理错误 return; 
 * } 
 * int size = opts.outWidth * opts.outHeight * 4;
 * boolean canLoad = BitmapMemoryHelper.isMemAvailable(size); 
 * if (canLoad) {
 * 	//真正把图片加载进内存 
 * } else { 
 * 	int sampleSize = BitmapMemoryHelper.measureSampleSize(opts); 
 * 	if (sampleSize == -1 || !BitmapMemoryHelper.isMemAvailable(size)) { 
 * 	//图片无法加载进内存 
 * 	//处理错误 
 * 	} else { 
 * 	size  = ((int)FloatMath.ceil((float)opts.outWidth / (float)sampleSize)) * ((int)FloatMath.ceil((float)opts.outHeight / (float)sampleSize)) * 4; 
 * 	if (!BitmapMemoryHelper.isMemAvailable(size)) { 
 * 		//图片无法加载进内存 
 * 		//处理错误 
 * 	} else {
 * 		//真正把图片加载进内存 
 * 	} 
 * 	} 
 * }
 * 
 */
public class BitmapMemoryHelper {

	private static final int MAX_SAMPLE_SIZE = 5;

	public static Context context = null;
	
	private static final float SAFETY_FACTOR = 0.8f;

	/**
	 * 检查内存是否够用
	 * 
	 * @param needMem
	 *            所需内存，单位Byte
	 * @return
	 */
	public static boolean isMemAvailable(int needMem) {
		ActivityManager mgr = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		int memClass = mgr.getMemoryClass();//unit is MB
		Debug.MemoryInfo info = mgr.getProcessMemoryInfo(new int[]{Process.myPid()})[0];
		int available = (int) ((memClass * 1024 * 1024 - info.dalvikPrivateDirty * 1024) * SAFETY_FACTOR);
		AdLogger.i("BitmapMemoryHelper","need mem is " + needMem + ", available is " + available);
		return available > needMem;
	}

	/**
	 * 获取最小的能够加载进入内存的sampleSize值
	 * 
	 * @param filePath
	 *            图片的地址
	 * @return 返回正数代表最小的sampleSize,返回-1代表无法加载进内存
	 */
	public static int measureSampleSize(String filePath) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		return measureSampleSize(opts);
	}

	/**
	 * 获取最小的能够加载进入内存的sampleSize值
	 * 
	 * @param stream
	 *            输入流，这个输入流应该能生成一个Bitmap
	 * @return 返回正数代表最小的sampleSize,返回-1代表无法加载进内存
	 */
	public static int measureSampleSize(InputStream stream) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeStream(stream, null, opts);
		return measureSampleSize(opts);
	}

	/**
	 * 获取最小的能够加载进入内存的sampleSize值
	 * 
	 * @param opts
	 *            已经decode过Bitmap尺寸的Options
	 * @return 返回正数代表最小的sampleSize,返回-1代表无法加载进内存
	 */
	public static int measureSampleSize(Options opts) {
		if (null == opts || null == opts.outMimeType) {
			return -1;
		}
		int size = opts.outWidth * opts.outHeight * 4;
		int sampleSize = 1;
		while (!isMemAvailable(size) && sampleSize <= MAX_SAMPLE_SIZE) {
			sampleSize++;
			float width = (float) opts.outWidth / (float) sampleSize;
			float height = (float) opts.outHeight / (float) sampleSize;
			int wth = (int) FloatMath.ceil(width);
			int hht = (int) FloatMath.ceil(height);
			size = wth * hht * 4;
		}
		if (!isMemAvailable(size)) {
			return -1;
		}
		return sampleSize;
	}

}
