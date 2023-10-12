package com.mercedesbenz.sechub.xraywrapper.cli;

import com.mercedesbenz.sechub.xraywrapper.api.XrayAPIArtifactoryClient;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperConfiguration;
import com.mercedesbenz.sechub.xraywrapper.report.XrayWrapperReportException;
import com.mercedesbenz.sechub.xraywrapper.report.XrayWrapperReportReader;
import org.cyclonedx.model.Bom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class XrayWrapperArtifactoryClientController {

    private static final Logger LOG = LoggerFactory.getLogger(XrayWrapperArtifactoryClientController.class);
    private final XrayWrapperConfiguration xrayWrapperConfiguration;
    private final XrayAPIArtifactoryClient artifactoryClient;
    private int retries;

    XrayWrapperReportReader reportReader;

    public XrayWrapperArtifactoryClientController(XrayWrapperConfiguration xrayWrapperConfiguration, XrayWrapperArtifact artifact) {
        this.xrayWrapperConfiguration = xrayWrapperConfiguration;
        this.retries = xrayWrapperConfiguration.getRequestRetries();
        this.artifactoryClient = new XrayAPIArtifactoryClient(artifact, xrayWrapperConfiguration);
        this.reportReader = new XrayWrapperReportReader();
    }

    /**
     * controls which http requests are send to the xray artifactory
     *
     * @throws XrayWrapperRuntimeException
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
        if (artifactoryClient.requestScanReports()) {
            manageReports();
        }

        // delete artifact from artifactory
        artifactoryClient.deleteArtifact();
        artifactoryClient.deleteUploads();
    }

    private void manageReports() throws XrayWrapperReportException {
        reportReader.getFiles(xrayWrapperConfiguration.getZip_directory(), xrayWrapperConfiguration.getSecHubReport());
        reportReader.readSecurityReport();
        Bom cycloneDXBom = reportReader.mapVulnerabilities();
        reportReader.writeReport(cycloneDXBom);
    }

    /**
     * checks on the scan status of the artifact in periodic time
     *
     * @return true if the artifact is scanned
     * @throws XrayWrapperRuntimeException
     */
    private boolean handleScanStatus() throws XrayWrapperRuntimeException {
        String status = artifactoryClient.getScanStatus();
        switch (status) {
        case "not scanned" -> {
            waitSeconds(xrayWrapperConfiguration.getWaitUntilRetrySec());
            if (0 >= retries) {
                LOG.info("Info: started artifact scan external");
                retries = xrayWrapperConfiguration.getRequestRetries();
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
            waitSeconds(xrayWrapperConfiguration.getWaitUntilRetrySec());
            LOG.info("Info: artifact scan in progress");
            return false;
        }
        case "scanned" -> {
            LOG.info("Info: artifact is scanned");
            return true;
        }
        }
        throw new XrayWrapperRuntimeException("Received unexspected scan status", XrayWrapperExitCode.UNSUPPORTED_API_REQUEST);
    }

    private static void waitSeconds(int val) {
        try {
            TimeUnit.SECONDS.sleep(val);
        } catch (InterruptedException e) {
            throw new XrayWrapperRuntimeException("Thread interrupted while wait for next scan status request", e, XrayWrapperExitCode.THREAD_INTERRUPTION);
        }
    }

}
