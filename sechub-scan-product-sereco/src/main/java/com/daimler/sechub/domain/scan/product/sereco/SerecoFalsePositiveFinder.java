// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Responsible class for identifying if a vulnerability identified by a product
 * is handled by a false positive meta data configuration
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class SerecoFalsePositiveFinder {

    @Autowired
    SerecoFalsePositiveCodeScanStrategy codeScanStrategy;
    
    @Autowired
    SerecoFalsePositiveWebScanStrategy webScanStrategy;
    
    public boolean isFound(SerecoVulnerability vulnerability, FalsePositiveMetaData metaData) {
        if (! isVulnerabilityValid(vulnerability)) {
            return false;
        }
        if (! isMetaDataValid(metaData)) {
            return false;
        }
        switch (vulnerability.getScanType()) {
        case CODE_SCAN:
            return codeScanStrategy.isFalsePositive(vulnerability, metaData);
        default:
            return false;
        }
    }
    
    private boolean isMetaDataValid(FalsePositiveMetaData metaData) {
        if (metaData == null) {
            return false;
        }
        String name = metaData.getName();
        if (name==null) {
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
        if (vulnerability.getType()==null) {
            return false;
        }
        return true;
    }

}
