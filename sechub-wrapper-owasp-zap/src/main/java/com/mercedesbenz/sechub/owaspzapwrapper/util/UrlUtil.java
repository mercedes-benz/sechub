// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.util;

import java.util.regex.Pattern;

import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;

public class UrlUtil {
    private static final String QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL = Pattern.quote(SecHubWebScanConfiguration.WEBSCAN_URL_WILDCARD_SYMBOL);
    private static final Pattern PATTERN_QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL = Pattern.compile(QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL);

    /**
     *
     * @param onlyForUrl
     * @return URL that contains a regular expression instead of wildcards or an
     *         empty String if the parameter was <code>null</code>.
     */
    public String replaceWildCardsWithRegexInUrl(String onlyForUrl) {
        if (onlyForUrl == null) {
            return "";
        }
        return PATTERN_QUOTED_WEBSCAN_URL_WILDCARD_SYMBOL.matcher(onlyForUrl).replaceAll(".*");
    }

}
