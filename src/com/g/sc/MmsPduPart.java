package com.g.sc;

import com.g.sc.MediaTypeRecogniser.MediaType;
import com.google.android.mms.pdu.PduPart;

public abstract class MmsPduPart extends PduPart {

	protected String name;
	public static final String SMIL = "smil";

	/**
	 * @param name
	 *            PDU的序号，传过来时为a-z的字母，要转换为1-26的字符串,或者 为"smil"，代表是smilpdu
	 * @param data
	 *            具体的数据
	 */
	public MmsPduPart(String name, String data) {
		char c = name.charAt(0);
		if (SMIL.equals(name)) {
			this.name = SMIL;
		} else {
			this.name = Integer.toString(c - 'a' + 1);
		}
		init(data);
	}

	public static boolean isSerialCorrect(String serial) {
		if (null == serial || serial.length() < 1) {
			return false;
		}
		if (SMIL.equals(serial)) {
			return true;
		}
		char c = serial.charAt(0);
		return c >= 'a' && c <= 'z';
	}

	public abstract MediaType getType();

	public abstract void init(String data);

}
