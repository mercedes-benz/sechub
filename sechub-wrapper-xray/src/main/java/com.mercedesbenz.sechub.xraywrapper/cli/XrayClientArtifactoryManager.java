package com.mercedesbenz.sechub.xraywrapper.cli;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIResponse;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.http.XrayHttpRequestExecutor;
import com.mercedesbenz.sechub.xraywrapper.http.XrayHttpRequestGenerator;
import com.mercedesbenz.sechub.xraywrapper.http.XrayHttpResponseBuilder;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayReportReader;

public class XrayClientArtifactoryManager {

    private XrayArtifact artifact;

    private XrayConfiguration xrayConfiguration;

    private int retries;

    public XrayClientArtifactoryManager(XrayConfiguration xrayConfiguration, XrayArtifact artifact) {
        this.xrayConfiguration = xrayConfiguration;
        this.artifact = artifact;
        this.retries = xrayConfiguration.getRequestRetries();
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
        reportReader.readReport(xrayConfiguration.getZip_directory(), xrayConfiguration.getSecHubReport());
    }

    private JsonNode getBodyAsNode(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(body);
    }

    private String getXrayVersion() throws IOException {
        XrayAPIRequest request = XrayHttpRequestGenerator.getXrayVersion(xrayConfiguration.getArtifactory());
        XrayAPIResponse response = send(request);
        JsonNode node = getBodyAsNode(response.getBody());
        return node.get("xray_version").asText();
    }

    private boolean checkArtifactoryUpload() throws IOException {
        XrayAPIRequest request = XrayHttpRequestGenerator.checkArtifactUpload(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        return !(isErrorResponse(response));
    }

    private String getScanStatus() throws IOException {
        XrayAPIRequest request = XrayHttpRequestGenerator.getScanStatus(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (!isErrorResponse(response)) {
            JsonNode node = getBodyAsNode(response.getBody());
            return node.get("status").asText();
        }
        return "error";
    }

    private boolean saveScanReportsSuccess() throws IOException {
        XrayAPIRequest request = XrayHttpRequestGenerator.getScanReports(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        return !isErrorResponse(response);
    }

    private String startScanArtifact() throws IOException {
        XrayAPIRequest request = XrayHttpRequestGenerator.scanArtifact(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
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
            waitSeconds(xrayConfiguration.getWaitUntilRetrySec());
            if (0 >= retries) {
                System.out.println("Started Xray scan...");
                retries = xrayConfiguration.getRequestRetries();
                startScanArtifact();
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

    private XrayAPIResponse send(XrayAPIRequest request) throws IOException {
        HttpURLConnection con = XrayHttpRequestExecutor.setUpGetConnection(request);
        return XrayHttpResponseBuilder.getHttpResponseFromConnection(con, xrayConfiguration.getZip_directory());
    }

}
