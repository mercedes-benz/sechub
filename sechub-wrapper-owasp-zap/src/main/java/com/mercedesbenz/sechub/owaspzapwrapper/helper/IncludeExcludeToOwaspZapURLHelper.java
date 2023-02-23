// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.owaspzapwrapper.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

public class IncludeExcludeToOwaspZapURLHelper {

    public List<URL> createListOfUrls(OwaspZapURLType urlType, URL targetUrl, List<String> subSites, List<SecHubMessage> userMessages) {
        if (subSites == null) {
            return new LinkedList<URL>();
        }

        List<URL> listOfUrls = new LinkedList<>();
        for (String subSite : subSites) {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(targetUrl);

            if (!subSite.startsWith("/")) {
                urlBuilder.append("/");
            }
            if (subSite.endsWith("/")) {
                urlBuilder.append(subSite.substring(0, subSite.length() - 1));
            } else {
                urlBuilder.append(subSite);
            }
            try {
                listOfUrls.add(new URL(urlBuilder.toString()));
            } catch (MalformedURLException e) {
                userMessages.add(new SecHubMessage(SecHubMessageType.ERROR, "The specified " + urlType.getId() + " " + subSite
                        + " combined with the target URL: " + targetUrl + " formed the invalid URL: " + urlBuilder.toString()));
            }
        }
        return listOfUrls;
    }

}
