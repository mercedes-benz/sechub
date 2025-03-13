// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.commons.core.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleStringUtils {

    public static final String EMPTY = "";
    private static final String TRUNCATE_POSTFIX = "...";
    private static final String OBFUSCATE_POSTFIX = "*****";

    private static final Logger LOG = LoggerFactory.getLogger(SimpleStringUtils.class);

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
        if (maxLength < 4) {
            throw new IllegalArgumentException("max length must be at least 4!");
        }
        if (string == null) {
            return null;
        }
        if (string.length() <= maxLength) {
            return string;
        }
        return string.substring(0, maxLength - TRUNCATE_POSTFIX.length()) + TRUNCATE_POSTFIX;
    }

    /**
     * obfuscates secret strings by showing only the required number of characters,
     * trunc the rest and add "*****"
     *
     * @param string
     * @param nonObfuscatedCharacters number of characters that should be shown in
     *                                clear text (eg. 3 -> abc*****), if the value
     *                                is negative, the original characters are shown
     *                                without postfix, if the value is greater than
     *                                the secret length, all characters are shown
     *                                with the postfix
     * @return obfuscated string
     */
    public static String createObfuscatedString(String string, int nonObfuscatedCharacters) {
        if (string == null) {
            return null;
        }

        if (nonObfuscatedCharacters < 0) {
            return string;
        }

        int remaining = nonObfuscatedCharacters;
        if (string.length() < remaining) {
            remaining = string.length();
        }

        return string.substring(0, remaining) + OBFUSCATE_POSTFIX;
    }

    /**
     * Checks if given character is a standard ascii letter (A-Z) or (a-z) (no
     * umlauts etc.)
     *
     * @param c character to check
     * @return <code>true</code> when simple latin letter <code>false</code> when
     *         not
     */
    public static boolean isStandardAsciiLetter(char c) {
        if (c >= 65 && c <= 90) {
            /* latin capital letter */
            return true;
        }
        if (c >= 97 && c <= 122) {
            /* latin lower letter */
            return true;
        }
        return false;
    }

    /**
     * Will test if given string does contain only alphabetic characters (a-z,A-Z),
     * digits (0-9) or additionally allowed characters.
     *
     * @param string            the content to inspect.
     * @param additionalAllowed
     * @return <code>true</code> when only allowed characters are contained, or
     *         given string is <code>null</code> or empty. <code>false</code>
     *         otherwise.
     */
    public static boolean hasStandardAsciiLettersDigitsOrAdditionalAllowedCharacters(String string, char... additionalAllowed) {
        if (string == null) {
            return true;
        }
        if (string.isEmpty()) {
            return true;
        }
        for (char c : string.toCharArray()) {
            if (Character.isDigit(c)) {
                continue;
            }
            if (isStandardAsciiLetter(c)) {
                continue;
            }
            boolean ok = false;
            for (char allowed : additionalAllowed) {
                if (c == allowed) {
                    ok = true;
                    continue;
                }
            }
            if (ok) {
                continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Creates a list for comma separated string. Values will be automatically
     * trimmed.<br>
     * <br>
     * Example:
     *
     * <pre>
     * The string "a,b,c,de, f" will be transformed to a list containing strings:
     *
     *  -"a"
     *  -"b"
     *  -"c"
     *  -"de"
     *  -"f".
     * </pre>
     *
     * @param string
     * @return list with values, never <code>null</code>
     */
    public static List<String> createListForCommaSeparatedValues(String string) {
        List<String> patterns = new ArrayList<>();
        if (string == null) {
            return patterns;
        }
        StringTokenizer tokenizer = new StringTokenizer(string, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (!token.isEmpty()) {
                patterns.add(token);
            }
        }
        return patterns;
    }

    /**
     * Tries to convert given value to an integer. If this is not possible the
     * defined default value will be returned instead. If the value is not empty,
     * but conversion is not possible a conversion problem is logged.
     *
     * @param value        the string value to convert
     * @param defaultValue the fallback value if the conversion is not possible.
     * @return integer value
     */
    public static int toIntOrDefault(String value, int defaultValue) {
        int result = defaultValue;
        if (value != null) {
            try {
                result = Integer.valueOf(value);
            } catch (NumberFormatException e) {
                LOG.warn("Was not able to convert value {} to an integer, so returning default value:{}", value, result);
            }
        }
        return result;

    }
}
