// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.notification.email;

import java.util.Map;
import java.util.TreeMap;

public class SMTPConfigStringToMapConverter {

    public Map<String, String> convertToMap(String smtpConfigString) {
        Map<String, String> map = new TreeMap<>();
        if (smtpConfigString == null) {
            return map;
        }

        String[] splitted = smtpConfigString.split(",");
        for (String splitter : splitted) {
            String[] keyValuePair = splitter.split("=");
            if (keyValuePair == null || keyValuePair.length != 2) {
                continue;
            }
            String key = keyValuePair[0];
            String value = keyValuePair[1];

            if (key == null || value == null) {
                continue;
            }

            map.put(key.trim(), value.trim());

        }
        return map;
    }

}
