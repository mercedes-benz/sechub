// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoClassification;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

@Component
public class SerecoFalsePositiveSupport {

    private static final Logger LOG = LoggerFactory.getLogger(SerecoFalsePositiveSupport.class);

    public boolean areBothHavingExpectedScanType(ScanType type, FalsePositiveMetaData metaData, SerecoVulnerability vulnerability) {
        notNull(vulnerability, " vulnerability may not be null");
        notNull(metaData, " metaData may not be null");

        /* check supported scan type */
        if (metaData.getScanType() != type) {
            return false;
        }

        if (vulnerability.getScanType() != type) {
            return false;
        }
        return true;
    }

    public boolean areBothHavingSameCweIdOrBothNoCweId(FalsePositiveMetaData metaData, SerecoVulnerability vulnerability) {
        notNull(vulnerability, " vulnerability may not be null");
        notNull(metaData, " metaData may not be null");

        Integer cweIdOrNull = metaData.getCweId();

        SerecoClassification serecoClassification = vulnerability.getClassification();
        String serecoCWE = serecoClassification.getCwe();
        if (serecoCWE == null || serecoCWE.isEmpty()) {
            if (cweIdOrNull == null) {
                /*
                 * when not set in meta data and also not in vulnerability, than we assume it is
                 * the same
                 */
                return true;
            }
            return false;
        }
        if (cweIdOrNull == null) {
            return false;
        }
        try {
            int serecoCWEint = Integer.parseInt(serecoCWE);
            if (cweIdOrNull.intValue() != serecoCWEint) {
                /* not same type of common vulnerability enumeration - so skip */
                return false;
            }

        } catch (NumberFormatException e) {
            LOG.error("Sereco vulnerability type:{} found CWE:{} but not expected integer format!", vulnerability.getType(), serecoCWE);
            return false;

        }
        return true;
    }
}
