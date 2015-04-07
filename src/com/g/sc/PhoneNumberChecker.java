package com.g.sc;

public class PhoneNumberChecker {

	public static class PhoneNumberError extends Exception {

		private static final long serialVersionUID = 5382485675163890295L;

		public PhoneNumberError(String message) {
			super(message);
		}
	}

	public static boolean isNumberCorrect(String number) {
		return !(number == null || "".equals(number));
	}
}
