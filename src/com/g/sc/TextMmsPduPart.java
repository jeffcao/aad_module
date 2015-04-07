package com.g.sc;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.g.sc.MediaTypeRecogniser.MediaType;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.CharacterSets;

public class TextMmsPduPart extends MmsPduPart {

	private static final String TAG = TextMmsPduPart.class.toString();

	public TextMmsPduPart(String name, String data) {
		super(name, data);
	}

	@Override
	public void init(String data) {
		AdLogger.i(TAG, "生成TEXT部件");
		AdLogger.i(TAG, "name is: " + name);
		AdLogger.i(TAG, "data is: " + data);
		this.setCharset(CharacterSets.UTF_8);
		this.setContentType(ContentType.TEXT_PLAIN.getBytes());
		this.setFilename(name.getBytes());
		String realData;
		try {
			realData = URLDecoder.decode(data, "utf-8");
		} catch (UnsupportedEncodingException e) {
			realData = URLDecoder.decode(data);
		}
		AdLogger.i(TAG, "realData is: " + realData);
		this.setData(realData.getBytes());
	}

	@Override
	public MediaType getType() {
		return MediaTypeRecogniser.MediaType.TXT;
	}
}
