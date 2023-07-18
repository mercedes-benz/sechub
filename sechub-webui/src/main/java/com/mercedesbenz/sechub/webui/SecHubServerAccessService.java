// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecHubServerAccessService {
    @Value("${sechub.server-url}")
    private String secHubServerUrl;

    @Value("${sechub.trust-all-certificates}")
    private boolean trustAllCertificates;

    public String getSecHubServerUrl() {
        return secHubServerUrl;
    }

    public void setSecHubServerUrl(String secHubServerUrl) {
        this.secHubServerUrl = secHubServerUrl;
    }

    public boolean isTrustAllCertificates() {
        return trustAllCertificates;
    }

    public void setTrustAllCertificates(boolean trustAllCertificates) {
        this.trustAllCertificates = trustAllCertificates;
    }
}
