// SPDX-License-Identifier: MIT
package com.daimler.sechub.developertools.admin.ui.util;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Why this class and not just using {@link java.util.Properties} ? Because we
 * want to have value content being JSON - and this makes problems in
 * properties. Also there are some special characters in
 * {@link java.util.Properties} which is not wanted.
 * 
 * @author Albert Tregnaghi
 *
 */
public class TextToSortedMapConverter {

    /**
     * Converts lines with "key=value" to a sorted map. Keys and values are
     * automatically trimmed! Empty keys or empty values are ignored
     * @param text
     * @return
     */
    public SortedMap<String, String> convertFromText(String text) {
        TreeMap<String, String> map = new TreeMap<>();
        if (text == null || text.isEmpty()) {
            return map;
        }
        
        String[] lines = text.split("\n");
        for (String line: lines) {
            int index = line.indexOf('=');
            if (index<=0) {
                continue;
            }
            if (line.length()<index+1) {
                continue;
            }
            String key = line.substring(0,index).trim();
            String val = line.substring(index+1).trim();
            
            if (key.isEmpty()) {
                continue;
            }
            if (val.isEmpty()) {
                continue;
            }
            map.put(key, val);
        }
        
        return map;
    }

}
