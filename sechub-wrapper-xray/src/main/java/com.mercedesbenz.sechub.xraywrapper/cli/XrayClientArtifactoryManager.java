package com.mercedesbenz.sechub.xraywrapper.cli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIResponse;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayReportReader;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class XrayClientArtifactoryManager {

    private final String baseUrl;

    private final String repository;

    private final String zipArchiveFilename;

    private final XrayDockerImage image;

    private final String pdsResultFile;

    private int wait = 10;
    private int retries = 6;

    public XrayClientArtifactoryManager(XrayConfiguration xrayConfiguration, XrayDockerImage image) {
        this.baseUrl = xrayConfiguration.getArtifactory();
        this.repository = xrayConfiguration.getRegister();
        this.zipArchiveFilename = xrayConfiguration.getZip_directory();
        this.image = image;
        this.pdsResultFile = xrayConfiguration.getSecHubReport();
    }

    /**
     * manages communication with the xray server in correct order
     *
     * @throws IOException
     */
    public void start() throws IOException {
        // performs all necessary API calls to get reports

        // get xray version from artifactory
        String xray_version = getXrayVersion();
        System.out.println("XRAY Version: " + xray_version);

        // check if artifact is uploaded
        boolean isUploaded = checkArtifactoryUpload();
        if (!isUploaded) {
            System.out.println("Error: Component could not be found in artifactory, was upload correctly?");
            System.exit(0);
        } else {
            System.out.println("Artifact successful uploaded");
        }

        String scanStatus = getScanStatus();
        boolean scanned = false;

        if (scanStatus.equals("error"))
            System.exit(0);

        while (!scanned) {
            scanned = handleScanStatus();
        }

        // save reports from xray
        if (saveScanReportsSuccess())
            manageReports();
    }

    private boolean isErrorResponse(XrayAPIResponse response) {
        response.print();
        if (response.getStatus_code() > 299) {
            // todo: Error logging
            System.out.println("Error Response:");
            System.out.println(response.getBody());
            return true;
        }
        return false;
    }

    private void manageReports() throws IOException {
        // hardcoded in response builder
        XrayReportReader reportReader = new XrayReportReader();
        reportReader.readReport(zipArchiveFilename, pdsResultFile);
    }

    private JsonNode getBodyAsNode(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(body);
    }

    private String getXrayVersion() throws IOException {
        XrayAPIRequest request;
        request = XrayAPIcalls.getXrayVersion(baseUrl);
        XrayAPIResponse response;
        response = request.sendRequest();
        JsonNode node = getBodyAsNode(response.getBody());
        return node.get("xray_version").asText();
    }

    private boolean checkArtifactoryUpload() throws IOException {
        XrayAPIRequest request;
        request = XrayAPIcalls.checkArtifactUpload(baseUrl, image, repository);
        XrayAPIResponse response;
        response = request.sendRequest();
        return !(isErrorResponse(response));
    }

    private String getScanStatus() throws IOException {
        XrayAPIRequest request;
        XrayAPIResponse response;
        request = XrayAPIcalls.getScanStatus(baseUrl, image, repository);
        response = request.sendRequest();
        if (!isErrorResponse(response)) {
            JsonNode node = getBodyAsNode(response.getBody());
            return node.get("status").asText();
        }
        return "error";
    }

    private boolean saveScanReportsSuccess() throws IOException {
        XrayAPIRequest request;
        XrayAPIResponse response;
        request = XrayAPIcalls.getScanReports(baseUrl, image, zipArchiveFilename);
        response = request.sendRequest();
        return !isErrorResponse(response);
    }

    private String startScanArtifact() throws IOException {
        XrayAPIRequest request;
        XrayAPIResponse response;
        request = XrayAPIcalls.scanArtifact(baseUrl, image, repository);
        response = request.sendRequest();
        if (!isErrorResponse(response)) {
            JsonNode node = getBodyAsNode(response.getBody());
            return node.get("info").asText();
        }
        return "";
    }

    private boolean handleScanStatus() throws IOException {
        String status = getScanStatus();
        switch (status) {
        case "not scanned" -> {
            waitSeconds(wait);
            if (0 >= retries) {
                System.out.println("Started Xray scan...");
                retries = 10;
                startScanArtifact();
            }
            retries--;
            System.out.println("Artifact not scanned...");
            return false;
        }
        case "in progress" -> {
            waitSeconds(wait);
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
