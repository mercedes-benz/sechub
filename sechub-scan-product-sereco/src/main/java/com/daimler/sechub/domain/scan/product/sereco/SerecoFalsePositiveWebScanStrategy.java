// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.domain.scan.project.FalsePositiveWebMetaData;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Strategy to check if a web scan vulnerability identified by a product is
 * handled by a false positive meta data configuration
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class SerecoFalsePositiveWebScanStrategy {

    @Autowired
    SerecoSourceRelevantPartResolver relevantPartResolver;

    private static final Logger LOG = LoggerFactory.getLogger(SerecoFalsePositiveWebScanStrategy.class);

    /**
     * Given data is supposed to be valid
     * 
     * @param vulnerability
     * @param metaData
     * @return true when identified as false positive
     */
    public boolean isFalsePositive(SerecoVulnerability vulnerability, FalsePositiveMetaData metaData) {
        notNull(vulnerability, " vulnerability may not be null");
        notNull(metaData, " metaData may not be null");

        if (metaData.getScanType() != ScanType.WEB_SCAN) {
            return false;
        }

        FalsePositiveWebMetaData metaDataWeb = metaData.getWeb();
        if (metaDataWeb == null) {
            LOG.error("Cannot check web vulnerability for false positives when meta data has no web parts!");
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------CWE ID--------------------------- */
        /* ---------------------------------------------------- */

        /* for web scans we only use CWE as wellknown common identifier */
        Integer cweId = metaData.getCweId();
        if (cweId == null) {
            LOG.error("Cannot check web vulnerability for false positives web code meta data has no CWE id set!");
            return false;
        }

        SerecoClassification serecoClassification = vulnerability.getClassification();
        String serecoCWE = serecoClassification.getCwe();
        if (serecoCWE == null || serecoCWE.isEmpty()) {
            LOG.error("Code scan sereco vulnerability type:{} found without CWE! Cannot determin false positive! Classification was:{}",
                    vulnerability.getType(), serecoClassification);
            return false;
        }
        try {
            int serecoCWEint = Integer.parseInt(serecoCWE);
            if (cweId.intValue()!=serecoCWEint) {
                /* not same type of common vulnerability enumeration - so skip */
                return false;
            }

        } catch (NumberFormatException e) {
            LOG.error("Code scan sereco vulnerability type:{} found CWE:{} but not expected integer format!", vulnerability.getType(), serecoCWE);
            return false;

        }

        return true;
    }

}
