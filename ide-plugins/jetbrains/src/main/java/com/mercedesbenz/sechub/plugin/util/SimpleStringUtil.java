// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.plugin.util;

/**
 * A class with different utility methods.
 */
public class SimpleStringUtil {

    /**
     * This method removes all space characters (spaces, tabs etc.) from a given
     * string.
     *
     * @return String without any space characters.
     */
    public static String removeAllSpaces(String stringWithSpaces) {
        return stringWithSpaces.replaceAll("\\s+", "");
    }

    /**
     * Returns a trimmed string
     *
     * @param object or null
     * @return trimmed toString() or "" when given string was null
     */
    public static String toStringTrimmed(Object object) {
        if (object == null) {
            return "";
        }
        return object.toString().trim();
    }
}
