// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Responsible class for identifying if a vulnerability identified by a product
 * is handled by a false positive meta data configuration
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SerecoJobDataFalsePositiveFinder {

    @Autowired
    CodeScanJobDataFalsePositiveStrategy jobDataCodeScanStrategy;

    @Autowired
    SecretScanJobDataFalsePositiveStrategy jobDataSecretScanStrategy;

    @Autowired
    IacScanJobDataFalsePositiveStrategy jobDataIacScanStrategy;

    @Autowired
    WebScanJobDataFalsePositiveStrategy jobDataWebScanStrategy;

    public boolean isFound(SerecoVulnerability vulnerability, FalsePositiveMetaData metaData) {
        if (!isVulnerabilityValid(vulnerability)) {
            return false;
        }
        if (!isMetaDataValid(metaData)) {
            return false;
        }
        ScanType scanType = vulnerability.getScanType();
        switch (scanType) {
        case CODE_SCAN:
            return jobDataCodeScanStrategy.isFalsePositive(vulnerability, metaData);
        case SECRET_SCAN:
            return jobDataSecretScanStrategy.isFalsePositive(vulnerability, metaData);
        case IAC_SCAN:
            return jobDataIacScanStrategy.isFalsePositive(vulnerability, metaData);
        case WEB_SCAN:
            return jobDataWebScanStrategy.isFalsePositive(vulnerability, metaData);
        default:
            return false;
        }
    }

    private boolean isMetaDataValid(FalsePositiveMetaData metaData) {
        if (metaData == null) {
            return false;
        }
        String name = metaData.getName();
        if (name == null) {
            return false;
        }
        return true;
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
