// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.core.util.SimpleStringUtils;
import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.domain.scan.project.FalsePositiveWebMetaData;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;
import com.daimler.sechub.sereco.metadata.SerecoWeb;
import com.daimler.sechub.sereco.metadata.SerecoWebAttack;
import com.daimler.sechub.sereco.metadata.SerecoWebEvidence;

/**
 * Strategy to check if a web scan vulnerability identified by a product is
 * handled by a false positive meta data configuration
 *
 * @author Albert Tregnaghi
 *
 */
@Component
public class SerecoFalsePositiveWebScanStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(SerecoFalsePositiveWebScanStrategy.class);

    /**
     * Checks if given vulnerability is identified as false positive by given meta
     * data
     *
     * @param vulnerability
     * @param metaData
     * @return <code>true</code> when identified as false positive
     */
    public boolean isFalsePositive(SerecoVulnerability vulnerability, FalsePositiveMetaData metaData) {
        notNull(vulnerability, " vulnerability may not be null");
        notNull(metaData, " metaData may not be null");

        /* check supported scan type */
        if (metaData.getScanType() != ScanType.WEB_SCAN) {
            return false;
        }

        if (vulnerability.getScanType() != ScanType.WEB_SCAN) {
            return false;
        }

        SerecoWeb vulnerabilityWeb = vulnerability.getWeb();
        if (vulnerabilityWeb == null) {
            LOG.error("Cannot check web vulnerability for false positives when vulnerability data has no web parts!");
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
            if (cweId.intValue() != serecoCWEint) {
                /* not same type of common vulnerability enumeration - so skip */
                return false;
            }

        } catch (NumberFormatException e) {
            LOG.error("Code scan sereco vulnerability type:{} found CWE:{} but not expected integer format!", vulnerability.getType(), serecoCWE);
            return false;

        }
        boolean sameData = true;
        /* ---------------------------------------------------- */
        /* -------------------Target--------------------------- */
        /* ---------------------------------------------------- */
        String metaTarget = metaDataWeb.getRequest().getTarget();
        String vulnerabilityTarget = vulnerabilityWeb.getRequest().getTarget();
        sameData = sameData && SimpleStringUtils.isTrimmedEqual(metaTarget, vulnerabilityTarget);

        /* ---------------------------------------------------- */
        /* -------------------HTTP Method---------------------- */
        /* ---------------------------------------------------- */
        String metaMethod = metaDataWeb.getRequest().getMethod();
        String vulnerabilityMethod = vulnerabilityWeb.getRequest().getMethod();
        sameData = sameData && SimpleStringUtils.isTrimmedEqual(metaMethod, vulnerabilityMethod);

        /* ---------------------------------------------------- */
        /* -------------------Attack vector-------------------- */
        /* ---------------------------------------------------- */
        String metaAttackVector = metaDataWeb.getRequest().getAttackVector();
        SerecoWebAttack attack = vulnerabilityWeb.getAttack();
        String vulnerabilityAttackVector = attack.getVector();
        sameData = sameData && SimpleStringUtils.isTrimmedEqual(metaAttackVector, vulnerabilityAttackVector);

        /* ---------------------------------------------------- */
        /* -------------------Evidence------------------------- */
        /* ---------------------------------------------------- */
        String metaEvidence = metaDataWeb.getResponse().getEvidence();
        SerecoWebEvidence evidence = attack.getEvidence();
        String vulnerabilityEvidence = null;
        if (evidence != null) {
            vulnerabilityEvidence = evidence.getSnippet();
        }
        sameData = sameData && SimpleStringUtils.isTrimmedEqual(metaEvidence, vulnerabilityEvidence);

        return sameData;
    }
}
