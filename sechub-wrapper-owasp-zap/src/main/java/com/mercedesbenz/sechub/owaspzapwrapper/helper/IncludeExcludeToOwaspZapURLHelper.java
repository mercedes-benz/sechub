// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import java.util.LinkedList;
import java.util.List;

public class IncludeExcludeToOwaspZapURLHelper {

    public List<String> createListOfOwaspZapCompatibleUrls(String targetUrl, List<String> subSites) {
        if (subSites == null) {
            return new LinkedList<String>();
        }

        List<String> listOfUrls = new LinkedList<>();
        for (String url : subSites) {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(targetUrl);

            if (!url.startsWith("/")) {
                urlBuilder.append("/");
            }

            if (url.endsWith("/")) {
                urlBuilder.append(url.substring(0, url.length() - 1));
            } else {
                urlBuilder.append(url);
            }
            listOfUrls.add(urlBuilder.toString());
        }
        return listOfUrls;
    }

}
