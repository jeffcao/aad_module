package com.yyxu.download.utils;

import com.g.sc.AdLogger;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

public class BitmapUtil {
	public static Bitmap scale(Bitmap bmp, float width_scale, float height_scale) {
		int bmpWidth = bmp.getWidth();
		int bmpHeight = bmp.getHeight();
		AdLogger.i("BitmapUtil","original (" + bmpHeight + ", " + bmpWidth + ")");
		float scaleWidth = (float) (bmpWidth * width_scale);
		float scaleHeight = (float) (bmpHeight * height_scale);
		AdLogger.i("BitmapUtil","scale to (" + scaleHeight + ", " + scaleWidth + ")");
		/* 产生reSize后的Bitmap对象 */
		Matrix matrix = new Matrix();
		matrix.postScale(width_scale, height_scale);
		Bitmap resizeBmp = Bitmap.createBitmap(bmp, 0, 0, bmpWidth, bmpHeight,
				matrix, true);
		// bmp.recycle();
		return resizeBmp;
	}

	/**
	 * 画一个圆角图
	 * 
	 * @param bitmap
	 * @param roundPx
	 * @return
	 */
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(output);
		int color = 0xff424242;
		Paint paint = new Paint();
		Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		RectF rectF = new RectF(rect);
		paint.setAntiAlias(true);
		canvas.drawARGB(0, 0, 0, 0);
		paint.setColor(color);
		canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
		paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);
		return output;
	}

}
