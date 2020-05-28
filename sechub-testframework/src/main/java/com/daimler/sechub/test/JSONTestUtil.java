// SPDX-License-Identifier: MIT
package com.daimler.sechub.test;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JSONTestUtil {
    /**
     * Creates a JSON containing also null values (JSONStringer does not support
     * this).
     * 
     * @param map
     * @return JSON string
     */
    public static String toJSONContainingNullValues(List<Map<String, Object>> list) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("[");
        for (Iterator<Map<String, Object>> it = list.iterator();it.hasNext();) {
            sb.append("\n");
            Map<String, Object> map = it.next();
            sb.append(toJSONContainingNullValues(map));
            if (it.hasNext()) {
                sb.append(",\n");
            }
        }
        sb.append("]\n");
        return sb.toString();
    }
    /**
     * Creates a JSON containing also null values (JSONStringer does not support
     * this).
     * 
     * @param map
     * @return JSON string
     */
    @SuppressWarnings("unchecked")
    public static String toJSONContainingNullValues(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();

        sb.append("{\n");
        Iterator<String> it = map.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            sb.append("\"");
            sb.append(key);
            sb.append("\" : ");
            Object value = map.get(key);
            if (value == null) {
                sb.append("null");
            } else {
                if (value instanceof Number) {
                    sb.append(value);
                } else if (value instanceof Map) {
                    sb.append(toJSONContainingNullValues((Map<String, Object>) value));
                } else if (value instanceof Boolean) {
                    boolean bool = (Boolean) value;
                    sb.append(bool);
                } else {
                    sb.append("\"");
                    sb.append(value);
                    sb.append("\"");
                }
            }
            if (it.hasNext()) {
                sb.append(",");
            }
            sb.append("\n");
        }
        sb.append("}");

        return sb.toString();
    }
}
