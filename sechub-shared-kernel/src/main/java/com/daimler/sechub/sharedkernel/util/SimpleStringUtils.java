// SPDX-License-Identifier: MIT
package com.daimler.sechub.sharedkernel.util;

public class SimpleStringUtils {

	private SimpleStringUtils() {
	}
	
	public static  boolean equals(String str1, String str2) {
		if (str1==null && str2==null) {
			return true;
		}
		if (str1==null || str2==null) {
			return false;
		}
		return str1.equals(str2);
	}

	public static boolean startsWith(String part, String full) {
		if (part==null && full==null) {
			return true;
		}
		if (part==null || full==null) {
			return false;
		}
		return full.startsWith(part);
	}
}
