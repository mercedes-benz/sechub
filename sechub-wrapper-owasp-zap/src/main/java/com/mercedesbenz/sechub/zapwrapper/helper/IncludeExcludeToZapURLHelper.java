// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.zapwrapper.helper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import com.mercedesbenz.sechub.commons.model.SecHubMessage;
import com.mercedesbenz.sechub.commons.model.SecHubMessageType;

public class IncludeExcludeToZapURLHelper {

    /**
     * Combine the targetUrl with all list of subSites.<br>
     * <br>
     * E.g. for the targetUrl http://localhost:8000 and the sub sites ["/api/v1/",
     * "admin/profile"], results in ["http://localhost:8000/api/v1",
     * "http://localhost:8000/admin/profile"].
     *
     * @param urlType
     * @param targetUrl
     * @param subSites
     * @param userMessages
     * @return a list of full URLs
     */
    public List<URL> createListOfUrls(ZapURLType urlType, URL targetUrl, List<String> subSites, List<SecHubMessage> userMessages) {
        if (subSites == null) {
            return new LinkedList<URL>();
        }

        String targetUrlAsString = targetUrl.toString();
        List<URL> listOfUrls = new LinkedList<>();
        for (String subSite : subSites) {
            StringBuilder urlBuilder = new StringBuilder();

            if (targetUrlAsString.endsWith("/")) {
                urlBuilder.append(targetUrlAsString.substring(0, targetUrlAsString.length() - 1));
            } else {
                urlBuilder.append(targetUrlAsString);
            }

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
