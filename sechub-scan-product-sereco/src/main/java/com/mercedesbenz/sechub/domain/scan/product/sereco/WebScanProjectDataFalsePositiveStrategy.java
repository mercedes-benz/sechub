// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.product.sereco;

import static com.mercedesbenz.sechub.sharedkernel.util.Assert.notNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.mercedesbenz.sechub.commons.model.ScanType;
import com.mercedesbenz.sechub.domain.scan.project.FalsePositiveProjectData;
import com.mercedesbenz.sechub.domain.scan.project.WebscanFalsePositiveProjectData;
import com.mercedesbenz.sechub.sereco.metadata.SerecoVulnerability;
import com.mercedesbenz.sechub.sereco.metadata.SerecoWeb;

@Component
public class WebScanProjectDataFalsePositiveStrategy implements SerecoFalsePositiveProjectDataStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(WebScanProjectDataFalsePositiveStrategy.class);

    @Autowired
    SerecoWebScanFalsePositiveProjectDataSupport webscanFalsePositiveProjectDataSupport;

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

        String target = vulnerabilityWeb.getRequest().getTarget();
        if (target == null) {
            return false;
        }

        URL targetUrl = null;
        try {
            targetUrl = new URL(target.trim());
        } catch (MalformedURLException e) {
            LOG.error("Sereco vulnerability webscan target URL: {} is not a valid URL!", target, e);
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------SERVERS-------------------------- */
        /* ---------------------------------------------------- */
        String host = targetUrl.getHost();
        if (!webscanFalsePositiveProjectDataSupport.isMatchingHostPattern(host, webScanData.getHostPatterns(), projectDataPatternMap)) {
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------URL PATTERNS--------------------- */
        /* ---------------------------------------------------- */
        // Using targetUrl.getFile() returns the path+query, maybe getPath() without
        // query would be better
        String urlFilePart = targetUrl.getFile();
        if (!webscanFalsePositiveProjectDataSupport.isMatchingUrlPathPattern(urlFilePart, webScanData.getUrlPathPatterns(), projectDataPatternMap)) {
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------METHODS-------------------------- */
        /* ---------------------------------------------------- */
        String method = vulnerabilityWeb.getRequest().getMethod();
        if (!webscanFalsePositiveProjectDataSupport.isMatchingMethodOrIngoreIfNotSet(method, webScanData.getMethods())) {
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------PORTS---------------------------- */
        /* ---------------------------------------------------- */
        int targetUrlPort = targetUrl.getPort();
        String port = targetUrlPort != -1 ? "" + targetUrlPort : "" + targetUrl.getDefaultPort();
        if (!webscanFalsePositiveProjectDataSupport.isMatchingPortOrIngoreIfNotSet(port, webScanData.getPorts())) {
            return false;
        }

        /* ---------------------------------------------------- */
        /* -------------------PROTOCOLS------------------------ */
        /* ---------------------------------------------------- */
        String protocol = vulnerabilityWeb.getRequest().getProtocol();
        if (!webscanFalsePositiveProjectDataSupport.isMatchingProtocolOrIngoreIfNotSet(protocol, webScanData.getProtocols())) {
            return false;
        }

        return true;
    }

}
