// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.core.util.SimpleStringUtils;
import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveMetaData;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveWebMetaData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;

@Component
public class WebScanJobDataFalsePositiveStrategy implements SerecoJobDataFalsePositiveStrategy {

    @Autowired
    SerecoJobDataFalsePositiveSupport falsePositiveSupport;

    private static final Logger LOG = LoggerFactory.getLogger(WebScanJobDataFalsePositiveStrategy.class);

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

        if (!falsePositiveSupport.areBothHavingExpectedScanType(ScanType.WEB_SCAN, metaData, vulnerability)) {
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
        if (!falsePositiveSupport.areBothHavingSameCweIdOrBothNoCweId(metaData, vulnerability)) {
            return false;
        }
        boolean sameData = true;
        /* ---------------------------------------------------- */
        /* -------------------NetworkTarget--------------------------- */
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

        return sameData;
    }
}
