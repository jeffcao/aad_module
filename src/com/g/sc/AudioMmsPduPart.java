package com.g.sc;

import com.g.sc.MediaTypeRecogniser.MediaType;
import com.google.android.mms.ContentType;
import com.google.android.mms.pdu.CharacterSets;

public class AudioMmsPduPart extends MmsPduPart {

	private static final String TAG = AudioMmsPduPart.class.toString();

	public AudioMmsPduPart(String name, String data) {
		super(name, data);
	}

	@Override
	public void init(String data) {
		AdLogger.i(TAG, "生成AUDIO部件");
		AdLogger.i(TAG, "name is: " + name);
		AdLogger.i(TAG, "data is: " + data);
		this.setCharset(CharacterSets.UTF_8);
		this.setContentType(ContentType.AUDIO_UNSPECIFIED.getBytes());
		this.setFilename(name.getBytes());
		Base64Utility base = new Base64Utility();
		byte[] b = base.decode(data);
		this.setData(b);
	}

	@Override
	public MediaType getType() {
		return MediaTypeRecogniser.MediaType.AUDIO;
	}

}
