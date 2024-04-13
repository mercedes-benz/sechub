// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import com.mercedesbenz.sechub.zapwrapper.util.UrlUtil;

public class IncludeExcludeToZapURLHelper {
    private UrlUtil urlUtil = new UrlUtil();

    /**
     * Combine the targetUrl with the given list of subSites.<br>
     * <br>
     * E.g. for the targetUrl http://localhost:8000 and the sub sites ["/api/v1/",
     * "<*>admin/<*>", "api/users/"], results in ["http://localhost:8000/api/v1",
     * "http://localhost:8000/.*admin/.*", "http://localhost:8000/.*api/users/.*"].
     *
     * @param targetUrl, must never be <code>null</code>
     * @param subSites
     * @return an unmodifiable list of full URLs, or an empty list if subSites was
     *         empty.
     */
    public List<String> createListOfUrls(URL targetUrl, List<String> subSites) {
        Objects.requireNonNull(targetUrl);
        if (subSites == null) {
            return Collections.emptyList();
        }

        String targetUrlAsString = targetUrl.toString();
        List<String> listOfUrls = new LinkedList<>();
        for (String subSite : subSites) {
            StringBuilder urlBuilder = new StringBuilder();
            // append the base target url first
            if (targetUrlAsString.endsWith("/")) {
                urlBuilder.append(targetUrlAsString);
            } else {
                urlBuilder.append(targetUrlAsString);
                urlBuilder.append("/");
            }

            // replace wildcards with patterns
            String replacedSubsite = urlUtil.replaceWebScanWildCardsWithRegexInString(subSite);

            // create include/exclude URL pattern
            if (replacedSubsite.startsWith("/")) {
                urlBuilder.append(replacedSubsite.substring(1));
            } else {
                if (!replacedSubsite.startsWith(UrlUtil.REGEX_PATTERN_WILDCARD_STRING)) {
                    urlBuilder.append(UrlUtil.REGEX_PATTERN_WILDCARD_STRING);
                }
                urlBuilder.append(replacedSubsite);

                if (!replacedSubsite.endsWith(UrlUtil.REGEX_PATTERN_WILDCARD_STRING)) {
                    urlBuilder.append(UrlUtil.REGEX_PATTERN_WILDCARD_STRING);
                }
            }
            listOfUrls.add(urlBuilder.toString());
        }
        return Collections.unmodifiableList(listOfUrls);
    }

}
