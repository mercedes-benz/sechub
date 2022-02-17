// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import java.util.Objects;

public class SimpleStringUtils {

    private SimpleStringUtils() {
    }

    /**
     * Checks if trimmed strings are equal (but case sensitive).
     *
     * @param string1 when <code>null</code> the parameter will be treated like an
     *                empty string
     * @param string2 when <code>null</code> the parameter will be treated like an
     *                empty string
     * @return <code>true</code> when trimmed variants are equal
     */
    public static boolean isTrimmedEqual(String string1, String string2) {
        if (Objects.equals(string1, string2)) {
            return true;
        }

        if (string1 == null) {
            string1 = "";
        }
        if (string2 == null) {
            string2 = "";
        }

        string1 = string1.trim();
        string2 = string2.trim();

        return string1.equals(string2);
    }

    public static boolean isNotEmpty(Object object) {
        return !isEmpty(object);
    }

    /**
     * Check whether the given object (possibly a {@code String}) is empty.
     */
    public static boolean isEmpty(Object str) {
        return (str == null || "".equals(str));
    }

    public static boolean equals(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    public static boolean startsWith(String part, String full) {
        if (part == null && full == null) {
            return true;
        }
        if (part == null || full == null) {
            return false;
        }
        return full.startsWith(part);
    }

    /**
     * Converts empty strings to null
     *
     * @param value
     * @return value when value is not <code>null</code> or empty, otherwise
     *         <code>null</code>
     */
    public static String emptyToNull(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return value;
    }

    /**
     * Will check if given string contains not more than allowed max length. If more
     * than max length string will be truncated and got a trailing with "..."
     *
     * @param string
     * @param maxLength , may not be smaller than 4
     * @return truncated length
     * @throws IllegalArgumentException when max length is smaller than 4
     */
    public static String truncateWhenTooLong(String string, int maxLength) {
        if (string == null) {
            return null;
        }
        if (maxLength < 4) {
            throw new IllegalArgumentException("max length must be at least 4!");
        }
        if (string.length() <= maxLength) {
            return string;
        }
        return string.substring(0, maxLength - 3) + "...";
    }
}
