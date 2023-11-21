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

    XrayWrapperReportSupport reportReader;

    public enum ScanStatus {
        SCANNED("scanned"),

        IN_PROGRESS("in progress"),

        NOT_SCANNED("not scanned");

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
        this.reportReader = new XrayWrapperReportSupport();
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
        artifactoryClient.checkArtifactoryUploadSuccess();
        LOG.debug("Artifact successfully uploaded to artifactory");

        // check scan status
        boolean scanned = false;
        while (!scanned) {
            scanned = handleScanStatus(context);
        }

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

        XrayWrapperReportSupport.XrayReportFiles reportFiles = reportReader.collectXrayReportsInArchive(xrayWrapperConfiguration.getZipDirectory(),
                xrayWrapperConfiguration.getXrayPdsReport());
        xrayWrapperReportData.setSecurityReport(reportFiles.securityReport());
        xrayWrapperReportData.setCycloneReport(reportFiles.cycloneReport());
        xrayWrapperReportData.setXrayPdsReport(reportFiles.xrayPdsReport());

        xrayWrapperReportData.setCycloneDXVulnerabilityHashMap(reportReader.readSecurityReport(xrayWrapperReportData.getSecurityReport()));
        xrayWrapperReportData
                .setSbom(reportReader.mapVulnerabilities(xrayWrapperReportData.getCycloneReport(), xrayWrapperReportData.cycloneDXVulnerabilityHashMap));
        reportReader.writeReport(xrayWrapperReportData.getSbom(), xrayWrapperReportData.getXrayPdsReport());
    }

    /**
     * checks on the scan status of the artifact in periodically
     *
     * @return true if the artifact is scanned
     * @throws XrayWrapperException
     */
    private boolean handleScanStatus(ClientSupportContext context) throws XrayWrapperException {
        ScanStatus status = artifactoryClient.getScanStatus();
        LOG.debug("Artifact status is: " + status.getStatusValue());
        if (context.isTimeoutReached()) {
            throw new XrayWrapperException("Reached maximum scan timeout of " + xrayWrapperConfiguration.getMaxScanDurationHours() + " hours. "
                    + "Started scan at: " + context.startTime, XrayWrapperExitCode.TIMEOUT_REACHED);
        }
        switch (status) {
        case NOT_SCANNED -> {
            waitSeconds(xrayWrapperConfiguration.getWaitUntilRetrySeconds());
            if (context.isMaximumRetriesReached()) {
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

    private static void waitSeconds(int val) throws XrayWrapperException {
        try {
            TimeUnit.SECONDS.sleep(val);
        } catch (InterruptedException e) {
            throw new XrayWrapperException("Thread interrupted while wait for next scan status request", XrayWrapperExitCode.THREAD_INTERRUPTION, e);
        }
    }

    private void startNewScan() throws XrayWrapperException {
        LOG.debug("Started new Artifact scan");
        artifactoryClient.startArtifactScan();
    }

    private class ClientSupportContext {
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
            LocalDateTime timer = now.plusHours(xrayWrapperConfiguration.getMaxScanDurationHours());
            return startTime.isAfter(timer);
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
