// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.sereco.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.mercedesbenz.sechub.commons.model.HTTPHeaderConfiguration;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.commons.model.SecHubConfigurationModel;
import com.mercedesbenz.sechub.commons.model.SecHubWebScanConfiguration;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;

@Service
public class SensitiveDataMaskingService {

    public static final String SENSITIVE_DATA_MASK = "********";

    /**
     *
     * Mask the sensitive information inside the given list of
     * SerecoVulnerabilities. E.g. HTTP headers the user configured, but contain
     * credential data. Each header configured in the sechub config contains a flag,
     * that specifies if the header is sensitive and should be masked.
     *
     * @param sechubConfig
     * @param vulnerabilities
     * @return A list of SerecoVulnerabilities where the sensitive information are
     *         masked.
     * @throws IllegalArgumentException if the parameter sechubConfig or the
     *                                  parameter vulnerabilities is null
     */
    public List<SerecoVulnerability> maskSensitiveData(SecHubConfigurationModel sechubConfig, List<SerecoVulnerability> vulnerabilities) {
        if (sechubConfig == null) {
            throw new IllegalArgumentException("Cannot mask sensitive data because the sechub configuration was null!");
        }
        if (vulnerabilities == null) {
            throw new IllegalArgumentException("Cannot mask sensitive data because the list of sereco vulnerabilities is null!");
        }

        if (sechubConfig.getWebScan().isEmpty()) {
            return vulnerabilities;
        }
        SecHubWebScanConfiguration webScanConfig = sechubConfig.getWebScan().get();
        if (webScanConfig.getHeaders().isEmpty()) {
            return vulnerabilities;
        }

        return maskSensitiveHeaderData(webScanConfig.getHeaders().get(), vulnerabilities);
    }

    private List<SerecoVulnerability> maskSensitiveHeaderData(List<HTTPHeaderConfiguration> httpHeaderConfigurations,
            List<SerecoVulnerability> vulnerabilities) {
        List<SerecoVulnerability> maskedVulnerabilities = new ArrayList<>();
        for (SerecoVulnerability serecoVulnerability : vulnerabilities) {
            maskedVulnerabilities.add(maskHeadersInSerecoVulnerability(httpHeaderConfigurations, serecoVulnerability));
        }

        return maskedVulnerabilities;
    }

    private SerecoVulnerability maskHeadersInSerecoVulnerability(List<HTTPHeaderConfiguration> httpHeaderConfigurations,
            SerecoVulnerability serecoVulnerability) {
        if (ScanType.WEB_SCAN != serecoVulnerability.getScanType()) {
            return serecoVulnerability;
        }
        SerecoWeb web = serecoVulnerability.getWeb();
        if (web == null) {
            return serecoVulnerability;
        }

        Map<String, String> requestHeaders = web.getRequest().getHeaders();
        Map<String, String> responseHeaders = web.getResponse().getHeaders();

        for (HTTPHeaderConfiguration httpHeaderConfiguration : httpHeaderConfigurations) {
            if (httpHeaderConfiguration.isSensitive()) {
                String headerName = httpHeaderConfiguration.getName();
                if (requestHeaders.containsKey(headerName)) {
                    requestHeaders.put(headerName, SENSITIVE_DATA_MASK);
                }
                if (responseHeaders.containsKey(headerName)) {
                    responseHeaders.put(headerName, SENSITIVE_DATA_MASK);
                }
            }
        }
        return serecoVulnerability;
    }

}
