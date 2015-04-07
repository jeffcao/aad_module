package com.yyxu.download.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.util.FloatMath;

import com.g.sc.AdLogger;

public class BitmapLoader {

	public static Bitmap load(String filePath) {
		return load(filePath, 1);
	}

	public static Bitmap load(String filePath, final int maxHeight,
			final int maxWidth) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		if (null == opts.outMimeType) {
			System.err.println("the file is not picture");
			return null;
		}
		float widthScale = opts.outWidth > maxWidth ? (float) opts.outWidth
				/ (float) maxWidth : 1f;
		float heightScale = opts.outHeight > maxHeight ? (float) opts.outHeight
				/ (float) maxHeight : 1f;
		int minSampleSize = (int) FloatMath.ceil(Math.max(widthScale,
				heightScale));
		return load(opts, minSampleSize, filePath);
	}

	public static Bitmap load(String filePath, final int minSampleSize) {
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, opts);
		return load(opts, minSampleSize, filePath);
	}

	public static Bitmap load(Options opts, final int minSampleSize,
			String filePath) {
		try {
			opts.inJustDecodeBounds = false;
			opts.inSampleSize = minSampleSize;
			AdLogger.i("BitmapLoader","load " + filePath
					+ ", in sample size at least " + minSampleSize);
			if (null == opts || null == opts.outMimeType) {
				// 给出的地址不是图片
				System.err.println("is not picture");
				return null;
			}
			int size = getSize(opts.outWidth, opts.outHeight, minSampleSize);
			boolean canLoad = BitmapMemoryHelper.isMemAvailable(size);
			if (canLoad) {
				// 真正把图片加载进内存
				AdLogger.i("BitmapLoader","can load directly");
				Bitmap bitmap = BitmapFactory.decodeFile(filePath, opts);
				AdLogger.i("BitmapLoader","bitmap is " + bitmap);
				return bitmap;
			} else {
				AdLogger.i("BitmapLoader","can not load directly");
				int sampleSize = BitmapMemoryHelper.measureSampleSize(opts);
				if (sampleSize == -1) {
					// 图片无法加载进内存
					System.err.println("can not load after add sample size");
					return null;
				} else {
					sampleSize = sampleSize > minSampleSize ? sampleSize
							: minSampleSize;
					size = getSize(opts.outWidth, opts.outHeight, sampleSize);
					if (!BitmapMemoryHelper.isMemAvailable(size)) {
						// 图片无法加载进内存
						System.err
								.println("can not load after get correct sample size");
						return null;
					} else {
						// 真正把图片加载进内存
						opts.inSampleSize = sampleSize;
						AdLogger.i("BitmapLoader","can load after add sample size "
								+ sampleSize);
						Bitmap bitmap = BitmapFactory
								.decodeFile(filePath, opts);
						return bitmap;
					}
				}
			}
		} catch (OutOfMemoryError e) {
			System.err
					.println("measure size is ok, but the oom exception is trhowed, catch it and return null bitmap");
			return null;
		}
	}

	public static Bitmap load(InputStream stream) {
		return load(stream, 1);
	}

	public static Bitmap load(InputStream stream, final int maxHeight,
			final int maxWidth) {
		byte[] is = getCopy(stream);
		if (null == is || is.length == 0) {
			System.err.println("get copy of stream error, return null");
			return null;
		}
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(is, 0, is.length, opts);
		if (null == opts.outMimeType) {
			return null;
		}
		float widthScale = opts.outWidth > maxWidth ? (float) opts.outWidth
				/ (float) maxWidth : 1f;
		float heightScale = opts.outHeight > maxHeight ? (float) opts.outHeight
				/ (float) maxHeight : 1f;
		int minSampleSize = (int) FloatMath.ceil(Math.max(widthScale,
				heightScale));
		return load(is, minSampleSize, opts);
	}

	public static Bitmap load(InputStream stream, final int minSampleSize) {
		byte[] is = getCopy(stream);
		if (null == is || is.length == 0) {
			System.err.println("get copy of stream error, return null");
			return null;
		}
		Options opts = new Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(is, 0, is.length, opts);

		return load(is, minSampleSize, opts);
	}

	private static Bitmap load(byte[] buffer, final int minSampleSize,
			Options opts) {
		try {
			opts.inJustDecodeBounds = false;
			opts.inSampleSize = minSampleSize;
			AdLogger.i("BitmapLoader","load " + buffer + ", in sample size at least "
					+ minSampleSize);
			if (null == opts || null == opts.outMimeType) {
				// 给出的地址不是图片
				System.err.println("is not picture");
				return null;
			}
			int size = getSize(opts.outWidth, opts.outHeight, minSampleSize);
			boolean canLoad = BitmapMemoryHelper.isMemAvailable(size);
			if (canLoad) {
				// 真正把图片加载进内存
				AdLogger.i("BitmapLoader","can load directly");
				Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0,
						buffer.length, opts);
				AdLogger.i("BitmapLoader","bitmap is " + bitmap);
				return bitmap;
			} else {
				AdLogger.i("BitmapLoader","can not load directly");
				int sampleSize = BitmapMemoryHelper.measureSampleSize(opts);
				if (sampleSize == -1) {
					// 图片无法加载进内存
					System.err.println("can not load after add sample size");
					return null;
				} else {
					sampleSize = sampleSize > minSampleSize ? sampleSize
							: minSampleSize;
					size = getSize(opts.outWidth, opts.outHeight, sampleSize);
					if (!BitmapMemoryHelper.isMemAvailable(size)) {
						// 图片无法加载进内存
						System.err
								.println("can not load after get correct sample size");
						return null;
					} else {
						// 真正把图片加载进内存
						opts.inSampleSize = sampleSize;
						AdLogger.i("BitmapLoader","can load after add sample size "
								+ sampleSize);
						Bitmap bitmap = BitmapFactory.decodeByteArray(buffer,
								0, buffer.length, opts);
						return bitmap;
					}
				}
			}
		} catch (OutOfMemoryError e) {
			System.err
					.println("measure size is ok, but the oom exception is trhowed, catch it and return null bitmap");
			return null;
		}
	}

	public static Bitmap load(Options opts, final int minSampleSize,
			InputStream stream) {
		return load(getCopy(stream), minSampleSize, opts);
	}

	private static int getSize(int width, int height, int sampleSize) {
		return ((int) FloatMath.ceil((float) width / (float) sampleSize))
				* ((int) FloatMath.ceil((float) height / (float) sampleSize))
				* 4;
	}

	private static byte[] getCopy(InputStream is) {
		if (null == is) {
			return null;
		}
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int read;
			while ((read = is.read(buffer)) != -1) {
				baos.write(buffer, 0, read);
			}
			baos.flush();
			return baos.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} catch (OutOfMemoryError oom) {
			oom.printStackTrace();
			return null;
		} finally {
			if (null != baos) {
				try {
					baos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
