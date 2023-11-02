package com.mercedesbenz.sechub.wrapper.xray.cli;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.cyclonedx.model.Bom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.api.XrayAPIArtifactoryClient;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;
import com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportReader;

public class XrayWrapperArtifactoryClientSupport {

    private static final Logger LOG = LoggerFactory.getLogger(XrayWrapperArtifactoryClientSupport.class);
    private final XrayWrapperConfiguration xrayWrapperConfiguration;
    private final XrayAPIArtifactoryClient artifactoryClient;

    XrayWrapperReportReader reportReader;

    public enum ScanStatus {
        SCANNED("scanned"), IN_PROGRESS("in progress"),

        NOT_SCANNED("not scanned");

        private String status;

        ScanStatus(String status) {
            this.status = status.toLowerCase();
        }

        public String getStatus() {
            return this.status;
        }

        public static ScanStatus fromString(String stringStatus) throws XrayWrapperException {
            for (ScanStatus status : ScanStatus.values()) {
                if (status.status.equals(stringStatus.toLowerCase())) {
                    return status;
                }
            }
            throw new XrayWrapperException("Received unexpected scan status", XrayWrapperExitCode.UNSUPPORTED_API_REQUEST);
        }
    }

    public XrayWrapperArtifactoryClientSupport(XrayWrapperConfiguration xrayWrapperConfiguration, XrayWrapperArtifact artifact) {
        this.xrayWrapperConfiguration = xrayWrapperConfiguration;
        this.artifactoryClient = new XrayAPIArtifactoryClient(artifact, xrayWrapperConfiguration);
        this.reportReader = new XrayWrapperReportReader();
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
        ClientControllerContext context = new ClientControllerContext();

        // get xray version from artifactory
        String xray_version = artifactoryClient.getXrayVersion();
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
        reportReader.findXrayReportsInArchive(xrayWrapperConfiguration.getZipDirectory(), xrayWrapperConfiguration.getSecHubReport());
        reportReader.readSecurityReport();
        Bom cycloneDXBom = reportReader.mapVulnerabilities();
        reportReader.writeReport(cycloneDXBom);
    }

    /**
     * checks on the scan status of the artifact in periodically
     *
     * @return true if the artifact is scanned
     * @throws XrayWrapperException
     */
    private boolean handleScanStatus(ClientControllerContext context) throws XrayWrapperException {
        ScanStatus status = artifactoryClient.getScanStatus();
        LOG.debug("Artifact status is: " + status.getStatus());
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

    private class ClientControllerContext {
        private final LocalDateTime startTime;
        private int retryCount;

        public ClientControllerContext() {
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

}
