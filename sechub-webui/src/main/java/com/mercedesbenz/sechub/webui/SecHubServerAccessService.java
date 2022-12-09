package com.mercedesbenz.sechub.webui;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SecHubServerAccessService {
    @Value("${sechub.serverUrl}")
    private String secHubServerUrl;

    @Value("${sechub.trustAllCertificates}")
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
