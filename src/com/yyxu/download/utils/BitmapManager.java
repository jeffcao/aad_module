package com.yyxu.download.utils;

import android.content.Context;

public class BitmapManager {

	public static void init(Context context, int defaultBitmapId) {
		BitmapMemoryHelper.context = context;
		WithDefaulBitmapLoader.context = context;
		WithDefaulBitmapLoader.DEFAULT_BITMAP_RESOURCE_ID = defaultBitmapId;
		WithDefaulBitmapLoader.getDefault();
	}

}
