// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

@Component
public class SerecoProjectDataFalsePositiveFinder {

    private final WebScanProjectDataFalsePositiveStrategy webScanProjectDataStrategy;

    public SerecoProjectDataFalsePositiveFinder(WebScanProjectDataFalsePositiveStrategy webScanProjectDataStrategy) {
        this.webScanProjectDataStrategy = webScanProjectDataStrategy;
    }

    public boolean isFound(SerecoVulnerability vulnerability, FalsePositiveProjectData projectData, Map<String, Pattern> projectDataPatternMap) {
        if (!isVulnerabilityValid(vulnerability)) {
            return false;
        }
        if (projectData == null) {
            return false;
        }
        if (projectDataPatternMap == null || projectDataPatternMap.isEmpty()) {
            return false;
        }

        ScanType scanType = vulnerability.getScanType();
        if (scanType == ScanType.WEB_SCAN) {
            return webScanProjectDataStrategy.isFalsePositive(vulnerability, projectData, projectDataPatternMap);
        }
        return false;
    }

    private boolean isVulnerabilityValid(SerecoVulnerability vulnerability) {
        if (vulnerability == null) {
            return false;
        }
        if (vulnerability.getScanType() == null) {
            return false;
        }
        if (vulnerability.getType() == null) {
            return false;
        }
        return true;
    }

}
