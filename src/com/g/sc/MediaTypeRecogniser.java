package com.g.sc;

public class MediaTypeRecogniser {

	public enum MediaType {
		SMIL, TXT, IMAGE, AUDIO, UNKWON
	};

	// --smil
	public static final String SMIL = "smil";
	
	// --text
	public static final String TXT = "txt";

	// --image
	public static final String JPG = "jpg";
	public static final String GIF = "gif";
	public static final String PNG = "png";
	public static final String JPEG = "jpeg";

	// --audio
	public static final String AAC = "aac";
	public static final String AMR = "anr";
	public static final String IMELODY = "imelody";
	public static final String MID = "mid";
	public static final String MIDI = "midi";
	public static final String MP3 = "mp3";
	public static final String MPEG3 = "mpeg3";
	public static final String MPEG = "mpeg";
	public static final String MPG = "mpg";
	public static final String MP4 = "mp4";
	public static final String TGPP = "3gpp";

	public static MediaType recogniseMediaType(String type) {
		if (null == type || "".equals(type)) {
			return MediaType.UNKWON;
		}
		if (isText(type)) {
			return MediaType.TXT;
		} else if (isSmil(type)) {
			return MediaType.SMIL;
		} else if (isImage(type)) {
			return MediaType.IMAGE;
		} else if (isAudio(type)) {
			return MediaType.AUDIO;
		}
		return MediaType.UNKWON;
	}
	
	private static boolean isSmil(String type) {
		return SMIL.equals(type);
	}

	private static boolean isText(String type) {
		return TXT.equals(type);
	}

	private static boolean isImage(String type) {
		return JPG.equals(type) || GIF.equals(type) || PNG.equals(type)
				|| JPEG.equals(type);
	}

	private static boolean isAudio(String type) {
		return AAC.equals(type) || AMR.equals(type) || IMELODY.equals(type)
				|| MID.equals(type) || MIDI.equals(type) || MP3.equals(type)
				|| MPEG3.equals(type) || MPEG.equals(type) || MPG.equals(type)
				|| MP4.equals(type) || TGPP.equals(type);
	}

}
