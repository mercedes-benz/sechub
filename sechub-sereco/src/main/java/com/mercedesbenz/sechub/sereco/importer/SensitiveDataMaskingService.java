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

    public List<SerecoVulnerability> maskSensitiveData(SecHubConfigurationModel sechubConfig, List<SerecoVulnerability> vulnerabilities) {
        if (sechubConfig.getWebScan().isEmpty()) {
            return vulnerabilities;
        }
        SecHubWebScanConfiguration webScanConfig = sechubConfig.getWebScan().get();
        if (webScanConfig.getHeaders().isEmpty()) {
            return vulnerabilities;
        }

        return maskSensitiveHeaderData(webScanConfig.getHeaders().get(), vulnerabilities);
    }

    private List<SerecoVulnerability> maskSensitiveHeaderData(List<HTTPHeaderConfiguration> httpHeaders, List<SerecoVulnerability> vulnerabilities) {
        List<SerecoVulnerability> maskedVulnerabilities = new ArrayList<>();
        for (SerecoVulnerability serecoVulnerability : vulnerabilities) {
            maskedVulnerabilities.add(maskHeadersInSerecoVulnerability(httpHeaders, serecoVulnerability));
        }

        return maskedVulnerabilities;
    }

    private SerecoVulnerability maskHeadersInSerecoVulnerability(List<HTTPHeaderConfiguration> httpHeaders, SerecoVulnerability serecoVulnerability) {
        if (ScanType.WEB_SCAN != serecoVulnerability.getScanType()) {
            return serecoVulnerability;
        }
        SerecoWeb web = serecoVulnerability.getWeb();
        if (web == null) {
            return serecoVulnerability;
        }

        Map<String, String> requestHeaders = web.getRequest().getHeaders();
        Map<String, String> responseHeaders = web.getResponse().getHeaders();

        for (HTTPHeaderConfiguration header : httpHeaders) {
            if (header.isSensitive()) {
                String headerName = header.getName();
                if (requestHeaders.containsKey(headerName)) {
                    requestHeaders.put(headerName, SENSITIVE_DATA_MASK);
                }
                if (responseHeaders.containsKey(headerName)) {
                    responseHeaders.put(headerName, SENSITIVE_DATA_MASK);
                }
            }
        }
        web.getRequest().getHeaders().putAll(requestHeaders);
        web.getResponse().getHeaders().putAll(responseHeaders);
        serecoVulnerability.setWeb(web);

        return serecoVulnerability;
    }

}
