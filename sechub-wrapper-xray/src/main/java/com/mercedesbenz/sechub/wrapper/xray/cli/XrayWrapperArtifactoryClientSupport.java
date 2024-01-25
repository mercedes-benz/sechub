// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.cyclonedx.model.Bom;
import org.cyclonedx.model.vulnerability.Vulnerability;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.api.XrayAPIArtifactoryClient;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;
import com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportSupport;

public class XrayWrapperArtifactoryClientSupport {

    private static final Logger LOG = LoggerFactory.getLogger(XrayWrapperArtifactoryClientSupport.class);
    private final XrayWrapperConfiguration xrayWrapperConfiguration;
    private final XrayAPIArtifactoryClient artifactoryClient;
    XrayWrapperReportSupport reportSupport;

    public enum ScanStatus {
        SCANNED("scanned"),

        IN_PROGRESS("in progress"),

        NOT_SCANNED("not scanned"),

        UNKNOWN("unknown");

        private String statusValue;

        ScanStatus(String status) {
            this.statusValue = status.toLowerCase();
        }

        public String getStatusValue() {
            return this.statusValue;
        }

        public static ScanStatus fromString(String stringStatus) throws XrayWrapperException {
            if (stringStatus == null) {
                throw new XrayWrapperException("Scan status is NULL", XrayWrapperExitCode.UNKNOWN_ERROR);
            }
            for (ScanStatus status : ScanStatus.values()) {
                if (status.statusValue.equals(stringStatus.toLowerCase())) {
                    return status;
                }
            }
            throw new XrayWrapperException("Received unexpected scan status", XrayWrapperExitCode.UNSUPPORTED_API_REQUEST);
        }
    }

    public XrayWrapperArtifactoryClientSupport(XrayWrapperConfiguration xrayWrapperConfiguration, XrayWrapperArtifact artifact) {
        this.xrayWrapperConfiguration = xrayWrapperConfiguration;
        this.artifactoryClient = new XrayAPIArtifactoryClient(artifact, xrayWrapperConfiguration);
        this.reportSupport = new XrayWrapperReportSupport();
    }

    /**
     * Controls which http requests are send to the xray artifactory, downloads the
     * reports after a successful scan and add security information from Security
     * report to the cycloneDX report further the artifacts are deleted from the
     * artifactory
     *
     * @throws XrayWrapperException
     */
    public void waitForScansToFinishAndDownloadReport() throws XrayWrapperException {
        ClientSupportContext context = new ClientSupportContext();

        // get xray version from artifactory
        String xray_version = artifactoryClient.requestXrayVersion();
        LOG.debug("Artifactory available, scan with Xray version: {}", xray_version);

        // check if artifact is uploaded
        artifactoryClient.assertArtifactoryUploadSuccess();
        LOG.debug("Artifact successfully uploaded to artifactory");

        // check scan status
        boolean scanned;
        do {
            scanned = requestAndHandleScanStatus(context);
        } while (!scanned);

        // save reports from xray
        if (artifactoryClient.requestScanReports()) {
            // rewrite cycloneDX report with security report information
            readAndConvertReports();
        }

        // delete artifact from artifactory
        artifactoryClient.deleteArtifact();
        artifactoryClient.deleteUploads();
    }

    private void readAndConvertReports() throws XrayWrapperException {
        XrayWrapperReportData xrayWrapperReportData = new XrayWrapperReportData();

        // get relevant xray report files
        XrayWrapperReportSupport.XrayReportFiles reportFiles = reportSupport.collectXrayReportsInArchive(xrayWrapperConfiguration.getZipDirectory(),
                xrayWrapperConfiguration.getXrayPdsReport());
        xrayWrapperReportData.setSecurityReport(reportFiles.securityReport());
        xrayWrapperReportData.setCycloneReport(reportFiles.cycloneReport());
        xrayWrapperReportData.setXrayPdsReport(reportFiles.xrayPdsReport());

        // extract vulnerabilities from security report
        xrayWrapperReportData.setCycloneDXVulnerabilityHashMap(reportSupport.readSecurityReport(xrayWrapperReportData.getSecurityReport()));

        // add extracted vulnerability information to the CycloneDX SBOM
        xrayWrapperReportData
                .setSbom(reportSupport.mapVulnerabilities(xrayWrapperReportData.getCycloneReport(), xrayWrapperReportData.getCycloneDXVulnerabilityHashMap()));

        // write new SBOM to file
        reportSupport.writeReport(xrayWrapperReportData.getSbom(), xrayWrapperReportData.getXrayPdsReport());
    }

    private boolean requestAndHandleScanStatus(ClientSupportContext context) throws XrayWrapperException {
        ScanStatus status = artifactoryClient.getScanStatus();

        context.setStatus(status);
        LOG.debug("Artifact status is: " + status.getStatusValue());
        if (context.isTimeoutReached()) {
            throw new XrayWrapperException("Reached maximum scan timeout of " + xrayWrapperConfiguration.getMaxScanDurationMinutes() + " minutes. "
                    + "Started scan at: " + context.startTime, XrayWrapperExitCode.TIMEOUT_REACHED);
        }

        switch (status) {
        case NOT_SCANNED -> {
            // note: after start scan there is a Xray delay between NOT_SCANNED and
            // IN_PROGRESS
            waitSeconds(xrayWrapperConfiguration.getWaitUntilRetrySeconds());
            if (context.isMaximumRetriesReached()) {
                // start new scan
                context.resetRetries();
                startNewScan();
            }
            context.markNextRetry();
            return false;
        }
        case IN_PROGRESS -> {
            waitSeconds(xrayWrapperConfiguration.getWaitUntilRetrySeconds());
            return false;
        }
        case SCANNED -> {
            return true;
        }
        }

        throw new XrayWrapperException("Received unexpected scan status", XrayWrapperExitCode.UNSUPPORTED_API_REQUEST);
    }

    private void waitSeconds(int val) {
        try {
            TimeUnit.SECONDS.sleep(val);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.info("Thread was interrupted while wait for next scan status request");
        }
    }

    private void startNewScan() throws XrayWrapperException {
        LOG.debug("Started new Artifact scan");
        artifactoryClient.startArtifactScan();
    }

    class ClientSupportContext {
        ScanStatus status;
        private final LocalDateTime startTime;
        private int retryCount;

        public ClientSupportContext() {
            startTime = LocalDateTime.now();
        }

        public boolean isMaximumRetriesReached() {
            return retryCount >= xrayWrapperConfiguration.getRequestRetries();
        }

        public void markNextRetry() {
            this.retryCount++;
        }

        public void resetRetries() {
            retryCount = 0;
        }

        public boolean isTimeoutReached() {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime timer = now.plusMinutes(xrayWrapperConfiguration.getMaxScanDurationMinutes());
            return startTime.isAfter(timer);
        }

        public ScanStatus getStatus() {
            return status;
        }

        public void setStatus(ScanStatus status) {
            this.status = status;
        }
    }

    private class XrayWrapperReportData {
        private File cycloneReport;
        private File securityReport;
        private File xrayPdsReport;
        private Map<String, Vulnerability> cycloneDXVulnerabilityHashMap;

        private Bom sbom;

        public File getCycloneReport() {
            return cycloneReport;
        }

        public void setCycloneReport(File cycloneReport) {
            this.cycloneReport = cycloneReport;
        }

        public File getSecurityReport() {
            return securityReport;
        }

        public void setSecurityReport(File securityReport) {
            this.securityReport = securityReport;
        }

        public File getXrayPdsReport() {
            return xrayPdsReport;
        }

        public void setXrayPdsReport(File xrayPdsReport) {
            this.xrayPdsReport = xrayPdsReport;
        }

        public Map<String, Vulnerability> getCycloneDXVulnerabilityHashMap() {
            return cycloneDXVulnerabilityHashMap;
        }

        public void setCycloneDXVulnerabilityHashMap(Map<String, Vulnerability> cycloneDXVulnerabilityHashMap) {
            this.cycloneDXVulnerabilityHashMap = cycloneDXVulnerabilityHashMap;
        }

        public Bom getSbom() {
            return sbom;
        }

        public void setSbom(Bom sbom) {
            this.sbom = sbom;
        }
    }

}
