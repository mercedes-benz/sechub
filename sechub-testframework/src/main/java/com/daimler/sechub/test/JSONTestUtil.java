package com.daimler.sechub.test;

import java.util.Iterator;
import java.util.Map;

public class JSONTestUtil {

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
