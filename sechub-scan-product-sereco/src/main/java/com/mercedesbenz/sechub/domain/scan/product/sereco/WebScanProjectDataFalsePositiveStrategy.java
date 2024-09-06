// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.notNull;

import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;

@Component
public class WebScanProjectDataFalsePositiveStrategy implements SerecoProjectDataFalsePositiveStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(WebScanProjectDataFalsePositiveStrategy.class);

    private final SerecoProjectDataWebScanFalsePositiveSupport webscanFalsePositiveProjectDataSupport;

    public WebScanProjectDataFalsePositiveStrategy(SerecoProjectDataWebScanFalsePositiveSupport webscanFalsePositiveProjectDataSupport) {
        this.webscanFalsePositiveProjectDataSupport = webscanFalsePositiveProjectDataSupport;
    }

    @Override
    public boolean isFalsePositive(SerecoVulnerability vulnerability, FalsePositiveProjectData projectData, Map<String, Pattern> projectDataPatternMap) {
        notNull(vulnerability, " vulnerability may not be null");
        notNull(projectData, " projectData may not be null");
        notNull(projectDataPatternMap, " projectDataPatternMap may not be null");

        // We use a fast exit approach in this method, since all conditions must be
        // satisfied, we exit as soon as possible.

        if (projectDataPatternMap.isEmpty()) {
            return false;
        }

        if (vulnerability.getScanType() != ScanType.WEB_SCAN) {
            return false;
        }

        WebscanFalsePositiveProjectData webScanData = projectData.getWebScan();
        if (webScanData == null) {
            return false;
        }

        SerecoWeb vulnerabilityWeb = vulnerability.getWeb();
        if (vulnerabilityWeb == null) {
            LOG.error("Cannot check web vulnerability for false positives when vulnerability data has no web parts!");
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------CWE ID--------------------------- */
        /* ---------------------------------------------------- */
        if (!webscanFalsePositiveProjectDataSupport.areBothHavingSameCweIdOrBothNoCweId(webScanData, vulnerability)) {
            return false;
        }

        String targetUrl = vulnerabilityWeb.getRequest().getTarget();
        if (targetUrl == null) {
            return false;
        }

        /* ---------------------------------------------------- */
        /* ----------------------URL--------------------------- */
        /* ---------------------------------------------------- */
        if (!webscanFalsePositiveProjectDataSupport.isMatchingUrlPattern(targetUrl, projectDataPatternMap)) {
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------METHODS-------------------------- */
        /* ---------------------------------------------------- */
        String method = vulnerabilityWeb.getRequest().getMethod();
        if (!webscanFalsePositiveProjectDataSupport.isMatchingMethodOrIgnoreIfNotSet(method, webScanData.getMethods())) {
            return false;
        }

        return true;
    }

}
