// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.commons.core;

import java.util.regex.Pattern;

public class PDSLogSanitizer {

    /**
     * This log sanitizer component handles -
     * https://cwe.mitre.org/data/definitions/93.html -
     * https://cwe.mitre.org/data/definitions/117.html
     *
     * @author Albert Tregnaghi
     *
     */

    private static final Pattern FORGERY_PATTERN = Pattern.compile("[\t\n\r]");

    public String sanitize(Object maybeContaminated, int maxLength) {
        String objAsString = null;
        if (maybeContaminated != null) {
            objAsString = maybeContaminated.toString();
        }
        return sanitize(objAsString, maxLength);
    }

    /**
     * Returns sanitized text.
     *
     * @param maybeContaminated
     * @param maxLength         when >0 the returned string has this maximum length.
     *                          use <=0 when length doesn't matter
     * @return sanitized text . Every character not being allowed will be replaced
     *         by <code>§</code> character. If string exceeds maximum length it will
     *         be truncated.
     */
    public String sanitize(String maybeContaminated, int maxLength) {
        return sanitize(maybeContaminated, maxLength, true);
    }

    /**
     * Returns sanitized text.
     *
     * @param maybeContaminated
     * @param maxLength         when >0 the returned string has this maximum length.
     *                          use <=0 when length doesn't matter
     * @param handleLogForgery  enables log forgery handling
     * @return sanitized text . when log forgery handling is enabled, every
     *         character not being allowed will be replaced by <code>§</code>
     *         character. If string exceeds maximum length it will be truncated.
     */
    public String sanitize(String maybeContaminated, int maxLength, boolean handleLogForgery) {
        if (maybeContaminated == null) {
            return null;
        }
        String result = maybeContaminated;
        if (maxLength > 0 && result.length() > maxLength) {
            result = result.substring(0, maxLength);
        }
        if (handleLogForgery) {
            result = FORGERY_PATTERN.matcher(result).replaceAll("§");
        }
        return result;
    }
}
