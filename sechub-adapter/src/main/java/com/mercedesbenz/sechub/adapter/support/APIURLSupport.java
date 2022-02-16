// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.support;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.adapter.AdapterConfig;

public class APIURLSupport {

    private static final Logger LOG = LoggerFactory.getLogger(APIURLSupport.class);

    private static final String DELIMITER = "/";

    public String createAPIURL(String apiPath, AdapterConfig config, String apiPrefix, String otherBaseURL, Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        String productBaseURL = config.getProductBaseURL();
        if (productBaseURL == null) {
            throw new IllegalStateException("product base URL is null!");
        }
        String base = (otherBaseURL == null ? productBaseURL : otherBaseURL);
        if (base.endsWith(DELIMITER)) {
            base = getSafeSubStringOneLeft(base);
        }
        if (apiPrefix == null) {
            apiPrefix = "";
        }
        if (apiPrefix.length() > 0 && !apiPrefix.startsWith(DELIMITER)) {
            apiPrefix = DELIMITER + apiPrefix;
        }
        if (apiPrefix.endsWith(DELIMITER)) {
            apiPrefix = getSafeSubStringOneLeft(apiPrefix);
        }
        if (apiPath == null) {
            apiPath = "";
        }
        if (apiPath.length() > 0 && !apiPath.startsWith(DELIMITER)) {
            apiPath = DELIMITER + apiPath;
        }
        if (apiPath.endsWith(DELIMITER)) {
            apiPath = getSafeSubStringOneLeft(apiPath);
        }
        sb.append(base);
        sb.append(apiPrefix);
        sb.append(apiPath);
        if (map == null || map.isEmpty()) {
            return sb.toString();
        }
        return appendQuery(map, sb);
    }

    private String appendQuery(Map<String, String> map, StringBuilder sb) {
        sb.append("?");
        for (Iterator<Entry<String, String>> it = map.entrySet().iterator(); it.hasNext();) {
            Entry<String, String> entry = it.next();
            sb.append(safe(entry.getKey()));
            sb.append('=');
            sb.append(safe(entry.getValue()));
            if (it.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    private String safe(String key) {
        if (key == null) {
            return "null";
        }
        try {
            return URLEncoder.encode(key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            LOG.error("Cannot encode api parts", e);
            return "illegal";
        }
    }

    private String getSafeSubStringOneLeft(String base) {
        int endIndex = base.length() - 1;
        if (endIndex < 0) {
            return "";
        }
        return base.substring(0, endIndex);
    }
}
