package com.g.sc;

import com.g.sc.MediaTypeRecogniser.MediaType;
import com.google.android.mms.ContentType;

public class SmilMmsPduPart extends MmsPduPart {

	private static final String TAG = SmilMmsPduPart.class.toString();

	public SmilMmsPduPart(String name, String data) {
		super(name, data);
	}

	@Override
	public void init(String data) {
		AdLogger.i(TAG, "生成SMIL部件");
		AdLogger.i(TAG, "smil is: " + data);
		setContentType(ContentType.APP_SMIL.getBytes());
		setData(data.getBytes());
	}

	@Override
	public MediaType getType() {
		return MediaTypeRecogniser.MediaType.SMIL;
	}

}
