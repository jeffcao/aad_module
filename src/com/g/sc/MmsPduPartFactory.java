package com.g.sc;

public class MmsPduPartFactory {

	/**
	 * 当type为UNKNOW时，返回空
	 * 
	 * @param type
	 * @param name
	 * @param data
	 * @return
	 */
	public static MmsPduPart create(MediaTypeRecogniser.MediaType type,
			String name, String data) {
		if (null == type || null == name || null == data
				|| !MmsPduPart.isSerialCorrect(name)) {
			return null;
		}
		MmsPduPart part = null;
		switch (type.ordinal()) {
		case 0:
			part = new SmilMmsPduPart(name, data);
			break;

		case 1:
			part = new TextMmsPduPart(name, data);
			break;

		case 2:
			part = new ImgMmsPduPart(name, data);
			break;

		case 3:
			part = new AudioMmsPduPart(name, data);
			break;

		case 4:
			// UNKNOW type, 返回空
			break;
		}
		return part;
	}

}
