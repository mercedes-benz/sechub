package com.mercedesbenz.sechub.xraywrapper.cli;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.http.XrayArtifactoryClient;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayReportReader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class XrayClientArtifactoryController {

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
    public void waitForScansToFinishAndDownloadReport() throws IOException {

        // get xray version from artifactory
        String xray_version = artifactoryClient.getXrayVersion();

        // check if artifact is uploaded
        boolean isUploaded = artifactoryClient.checkArtifactoryUpload();
        if (!isUploaded) {
            // todo xrayUploadException
            System.out.println("Error: Component could not be found in artifactory, was upload correctly?");
            System.exit(0);
        } else {
            System.out.println("Artifact successful uploaded");
        }

        String scanStatus = artifactoryClient.getScanStatus();
        boolean scanned = false;

        if (scanStatus.equals("error"))
            System.exit(0);

        while (!scanned) {
            scanned = handleScanStatus();
        }

        // save reports from xray
        if (artifactoryClient.requestScanReports())
            manageReports();
    }

    private void manageReports() throws IOException {
        // hardcoded in response builder
        XrayReportReader reportReader = new XrayReportReader();
        reportReader.getFiles(xrayConfiguration.getZip_directory(), xrayConfiguration.getSecHubReport());
        reportReader.readSecurityReport();
        ObjectNode root = reportReader.mapVulnerabilities();
        reportReader.writeReport(root);
    }

    private boolean handleScanStatus() throws IOException {
        String status = artifactoryClient.getScanStatus();
        switch (status) {
        case "not scanned" -> {
            waitSeconds(xrayConfiguration.getWaitUntilRetrySec());
            if (0 >= retries) {
                System.out.println("Started Xray scan...");
                retries = xrayConfiguration.getRequestRetries();
                artifactoryClient.startScanArtifact();
            }
            retries--;
            System.out.println("Artifact not scanned...");
            return false;
        }
        case "in progress" -> {
            waitSeconds(xrayConfiguration.getWaitUntilRetrySec());
            System.out.println("Scan in progress...");
            return false;
        }
        case "scanned" -> {
            System.out.println("Artifact already scanned");
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
