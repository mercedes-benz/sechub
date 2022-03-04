// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import java.util.LinkedList;
import java.util.List;

public class SecHubIncludeExcludeToOwaspZapURIHelper {

    public List<String> createListOfUrls(String targetUrl, List<String> subSites) {
        if (subSites == null) {
            return new LinkedList<String>();
        }

        List<String> listOfUrls = new LinkedList<>();
        for (String url : subSites) {
            StringBuilder buildUrl = new StringBuilder();
            buildUrl.append(targetUrl);

            if (!url.startsWith("/")) {
                buildUrl.append("/");
            }

            if (url.endsWith("/")) {
                buildUrl.append(url.substring(0, url.length() - 1));
            } else {
                buildUrl.append(url);
            }
            buildUrl.append(".*");

            listOfUrls.add(buildUrl.toString());
        }
        return listOfUrls;
    }

}
