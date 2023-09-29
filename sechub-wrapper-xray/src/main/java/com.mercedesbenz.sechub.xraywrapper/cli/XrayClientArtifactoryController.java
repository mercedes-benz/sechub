package com.mercedesbenz.sechub.xraywrapper.cli;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.http.XrayArtifactoryClient;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayReportReader;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayWrapperReportException;

public class XrayClientArtifactoryController {

    private static final Logger LOG = LoggerFactory.getLogger(XrayClientArtifactoryController.class);

    private final XrayConfiguration xrayConfiguration;
    private final XrayArtifactoryClient artifactoryClient;
    private int retries;

    public XrayClientArtifactoryController(XrayConfiguration xrayConfiguration, XrayArtifact artifact) {
        this.xrayConfiguration = xrayConfiguration;
        this.retries = xrayConfiguration.getRequestRetries();
        this.artifactoryClient = new XrayArtifactoryClient(artifact, xrayConfiguration);
    }

    /**
     * controls the communication to the Xray artifactory server
     *
     * @throws IOException
     */
    public void waitForScansToFinishAndDownloadReport() throws XrayWrapperRuntimeException {

        // get xray version from artifactory
        String xray_version = artifactoryClient.getXrayVersion();
        LOG.info("Info: artifactory available, scan with Xray version: {}", xray_version);

        // check if artifact is uploaded
        artifactoryClient.checkArtifactoryUpload();
        LOG.info("Info: artifact successfully uploaded to artifactory");

        // check scan status
        boolean scanned = false;
        while (!scanned) {
            scanned = handleScanStatus();
        }

        // save reports from xray
        if (artifactoryClient.requestScanReports())
            manageReports();
    }

    private void manageReports() throws XrayWrapperReportException {
        // hardcoded in response builder
        XrayReportReader reportReader = new XrayReportReader();
        reportReader.getFiles(xrayConfiguration.getZip_directory(), xrayConfiguration.getSecHubReport());
        reportReader.readSecurityReport();
        ObjectNode root = reportReader.mapVulnerabilities();
        reportReader.writeReport(root);
    }

    private boolean handleScanStatus() throws XrayWrapperRuntimeException {
        String status = artifactoryClient.getScanStatus();
        switch (status) {
        case "not scanned" -> {
            waitSeconds(xrayConfiguration.getWaitUntilRetrySec());
            if (0 >= retries) {
                LOG.info("Info: started artifact scan external");
                retries = xrayConfiguration.getRequestRetries();
                String started = artifactoryClient.startScanArtifact();
                if (!Objects.equals(started, "Scan of artifact is in progress")) {
                    LOG.error("Scan start was not successful");
                }
            }
            retries--;
            LOG.info("Info: artifact status is not scanned");
            return false;
        }
        case "in progress" -> {
            waitSeconds(xrayConfiguration.getWaitUntilRetrySec());
            LOG.info("Info: artifact scan in progress");
            return false;
        }
        case "scanned" -> {
            LOG.info("Info: artifact is scanned");
            return true;
        }
        }
        return false;
    }

    private static void waitSeconds(int val) {
        try {
            TimeUnit.SECONDS.sleep(val);
        } catch (InterruptedException e) {
            // log.error("Thread interrupted");
        }
    }

}
