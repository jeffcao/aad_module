package com.g.sc;

import java.io.File;
import java.io.IOException;

public class FileOperator {

	private static final String BLANK = "";

	public static File createDir(String path) {
		if (path == null || BLANK.equals(path)) {
			return null;
		}
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		return file;
	}

	public static File createFile(String path, String name) throws IOException {
		if (path == null || BLANK.equals(path) || name == null
				|| BLANK.equals(name)) {
			return null;
		}
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		return createFile(path + "/" + name);
	}

	public static File createFile(String path) throws IOException {
		if (path == null || BLANK.equals(path)) {
			return null;
		}
		File file = new File(path);
		if (!file.exists()) {
			file.createNewFile();
		}
		return file;
	}
}
