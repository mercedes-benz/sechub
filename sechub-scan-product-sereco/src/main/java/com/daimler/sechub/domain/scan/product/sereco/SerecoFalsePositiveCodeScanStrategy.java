// SPDX-License-Identifier: MIT
package com.daimler.sechub.domain.scan.product.sereco;

import static com.daimler.sechub.sharedkernel.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.daimler.sechub.commons.model.ScanType;
import com.daimler.sechub.domain.scan.project.FalsePositiveCodeMetaData;
import com.daimler.sechub.domain.scan.project.FalsePositiveCodePartMetaData;
import com.daimler.sechub.domain.scan.project.FalsePositiveMetaData;
import com.daimler.sechub.sereco.metadata.SerecoClassification;
import com.daimler.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.daimler.sechub.sereco.metadata.SerecoVulnerability;

/**
 * Strategy to check if a code scan vulnerability identified by a product is
 * handled by a false positive meta data configuration
 * 
 * @author Albert Tregnaghi
 *
 */
@Component
public class SerecoFalsePositiveCodeScanStrategy {

    @Autowired
    SerecoSourceRelevantPartResolver relevantPartResolver;

    private static final Logger LOG = LoggerFactory.getLogger(SerecoFalsePositiveCodeScanStrategy.class);

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

        if (metaData.getScanType() != ScanType.CODE_SCAN) {
            return false;
        }

        FalsePositiveCodeMetaData metaDataCode = metaData.getCode();
        if (metaDataCode == null) {
            LOG.error("Cannot check code vulnerability for false positives when meta data has no code parts!");
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------CWE ID--------------------------- */
        /* ---------------------------------------------------- */

        /* for code scans we only use CWE as wellknown common identifier */
        Integer cweId = metaData.getCweId();
        if (cweId == null) {
            LOG.error("Cannot check code vulnerability for false positives when code meta data has no CWE id set!");
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

        /* ------------------------------------------------------- */
        /* -------------------Location---------------------------- */
        /* ------------------------------------------------------- */
        SerecoCodeCallStackElement serecoFirstElement = vulnerability.getCode();
        if (serecoFirstElement == null) {
            /* strange - canot be investigated */
            LOG.warn("Cannot check code vulnerability for false positives when no first code element is found!");
            return false;
        }
        FalsePositiveCodePartMetaData start = metaDataCode.getStart();
        if (start == null) {
            LOG.warn("Cannot check code vulnerability for false positives when no start code meta data is found!");
            return false;
        }
        if (isLocationDifferent(start, serecoFirstElement)) {
            return false;
        }
        FalsePositiveCodePartMetaData end = metaDataCode.getEnd();
        SerecoCodeCallStackElement serecoLastElement = findLastElement(serecoFirstElement);
        if (isLocationDifferent(end, serecoLastElement)) {
            return false;
        }
        /* ------------------------------------------------------- */
        /* -------------------RELEVANT parts---------------------- */
        /* ------------------------------------------------------- */
        String relevant1 = start.getRelevantPart();
        String relevant2 = serecoFirstElement.getRelevantPart();

        if (relevant1 == null || relevant1.isEmpty()) {
            relevant1 = createRelevantReplacment(start);
        }
        if (relevant2 == null || relevant2.isEmpty()) {
            relevant2 = createRelevantReplacment(serecoFirstElement);
        }
        if (!relevant1.equals(relevant2)) {
            return false;
        }
        String relevant3 = "";
        String relevant4 = "";
        if (end != null) {
            relevant3 = end.getRelevantPart();
            if (relevant3 == null || relevant3.isEmpty()) {
                relevant3 = createRelevantReplacment(end);
            }
        }
        if (serecoLastElement != null) {
            relevant4 = serecoLastElement.getRelevantPart();
            if (relevant4 == null || relevant4.isEmpty()) {
                relevant4 = createRelevantReplacment(serecoLastElement);
            }
        }
        if (!relevant3.equals(relevant4)) {
            return false;
        }
        return true;
    }

    private String createRelevantReplacment(SerecoCodeCallStackElement serecoFirstElement) {
        return relevantPartResolver.toRelevantPart(serecoFirstElement.getSource());
    }

    private String createRelevantReplacment(FalsePositiveCodePartMetaData metaData) {
        return relevantPartResolver.toRelevantPart(metaData.getSourceCode());
    }

    private boolean isLocationDifferent(FalsePositiveCodePartMetaData metaData, SerecoCodeCallStackElement serecoElement) {
        if (metaData == null && serecoElement == null) {
            return false;
        }
        if (metaData == null) {
            return true;
        }
        if (serecoElement == null) {
            return true;
        }
        String location1 = metaData.getLocation();
        String location2 = serecoElement.getLocation();

        if (location1 == null || location2 == null) {
            LOG.warn("at least one location is null! should not be at this stage. location1:{}, location1:{}", location1, location2);
            return true; // we do not accept this at all
        }
        return !location2.equals(location1);
    }

    private SerecoCodeCallStackElement findLastElement(SerecoCodeCallStackElement serecoFirstElement) {
        SerecoCodeCallStackElement calls = serecoFirstElement;
        do {
            calls = calls.getCalls();
        } while (calls != null && calls.getCalls() != null);

        return calls;
    }

}
