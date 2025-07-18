// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.util;

/**
 * A class with different utility methods.
 *
 */
public class SimpleStringUtil {

	/**
	 * This method removes all space characters (spaces, tabs etc.) from a given string.
	 * 
	 * @return String without any space characters.
	 */
	public static String removeAllSpaces(String stringWithSpaces) {
		String stringWithNoSpaces = stringWithSpaces.replaceAll("\\s+", "");
		
		return stringWithNoSpaces;
	}
	
	public static String stringOrUnknown(String string) {
		return stringOrFallback(string, "unknown");
	}
	
	public static String stringOrFallback(String string, String fallback) {
		if (string!=null) {
			return string;
		}
		return fallback;
	}
}
