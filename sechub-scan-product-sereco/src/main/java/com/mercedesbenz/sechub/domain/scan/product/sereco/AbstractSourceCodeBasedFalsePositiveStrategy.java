// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveCodeMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveCodePartMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoCodeCallStackElement;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;

public abstract class AbstractSourceCodeBasedFalsePositiveStrategy implements SerecoJobDataFalsePositiveStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractSourceCodeBasedFalsePositiveStrategy.class);

    protected abstract ScanType getScanType();

    @Autowired
    SerecoSourceRelevantPartResolver relevantPartResolver;

    @Autowired
    SerecoJobDataFalsePositiveSupport falsePositiveSupport;

    public boolean isFalsePositive(SerecoVulnerability vulnerability, FalsePositiveMetaData metaData) {
        return isFalsePositive(getScanType(), vulnerability, metaData);
    }

    /**
     * Checks if given vulnerability is identified as false positive by given meta
     * data
     *
     * @param scanType
     * @param vulnerability
     * @param metaData
     * @return <code>true</code> when identified as false positive
     */
    protected boolean isFalsePositive(ScanType scanType, SerecoVulnerability vulnerability, FalsePositiveMetaData metaData) {
        notNull(vulnerability, " vulnerability may not be null");
        notNull(metaData, " metaData may not be null");
        notNull(scanType, " scanType may not be null");

        /* check supported scan type */
        if (!falsePositiveSupport.areBothHavingExpectedScanType(scanType, metaData, vulnerability)) {
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
        if (!falsePositiveSupport.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability)) {
            return false;
        }

        /* ------------------------------------------------------- */
        /* -------------------Location---------------------------- */
        /* ------------------------------------------------------- */
        return hasSameLocation(vulnerability, metaDataCode);
    }

    protected boolean hasSameLocation(SerecoVulnerability vulnerability, FalsePositiveCodeMetaData metaDataCode) {
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
        if (end == null) {
            /* only first element defined - so use this as last element */
            end = start;
        }
        SerecoCodeCallStackElement serecoLastElement = findLastElement(serecoFirstElement);
        if (serecoLastElement == null) {
            /* only first element defined - so use this as last element */
            serecoLastElement = serecoFirstElement;
        }
        if (isLocationDifferent(end, serecoLastElement)) {
            return false;
        }
        /* ------------------------------------------------------- */
        /* -------------------Relevant parts---------------------- */
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
