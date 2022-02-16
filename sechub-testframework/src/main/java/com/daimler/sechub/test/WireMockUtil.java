// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.util.Iterator;
import java.util.LinkedHashMap;

public class WireMockUtil {
    /**
     * Converts given map to form url encoded variant - unfortunately not directly
     * supported by Wiremock. So this util method was necessary. Just use it inside
     * your code with
     *
     * <pre>
     *  withRequestBody(equalTo(WireMockUtil.toFormUrlEncoded(map))).
     * </pre>
     *
     * @param map contains key values - we use a linked hash map to define expected
     *            ordering
     * @return url encoded variant (e.g. key1=value1&key2=value2)
     */
    public static String toFormUrlEncoded(LinkedHashMap<String, String> map) {
        return toFormUrlEncoded(map, false);
    }

    /**
     * Converts given map to form url encoded variant - unfortunately not directly
     * supported by Wiremock. So this util method was necessary. Just use it inside
     * your code with
     *
     * <pre>
     *  withRequestBody(equalTo(WireMockUtil.toFormUrlEncoded(map))).
     * </pre>
     *
     * @param map      contains key values - we use a linked hash map to define
     *                 expected ordering
     * @param withNull when <code>true</code> string "null" will be appended
     * @return url encoded variant (e.g. key1=value1&key2=value2)
     */
    public static String toFormUrlEncoded(LinkedHashMap<String, String> map, boolean withNull) {
        if (map == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            String value = map.get(key);

            appendFormUrlEncoded(key, value, sb, withNull);

            if (it.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    public static String toFormUrlEncoded(String key, String value) {
        StringBuilder sb = new StringBuilder();
        appendFormUrlEncoded(key, value, sb, false);
        return sb.toString();
    }

    public static void appendFormUrlEncoded(String key, String value, StringBuilder sb, boolean withNull) {
        sb.append(key).append('=');
        if (value != null) {
            sb.append(value);
        } else {
            if (withNull) {
                sb.append("null");
            }
        }
    }
}
