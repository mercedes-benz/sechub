// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

@Component
public class SerecoProjectDataFalsePositiveFinder {

    @Autowired
    WebScanProjectDataFalsePositiveStrategy webScanProjectDataStrategy;

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
        switch (scanType) {
        case WEB_SCAN:
            return webScanProjectDataStrategy.isFalsePositive(vulnerability, projectData, projectDataPatternMap);
        default:
            return false;
        }
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
