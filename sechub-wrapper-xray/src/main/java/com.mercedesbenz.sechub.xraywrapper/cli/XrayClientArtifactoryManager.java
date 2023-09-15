package com.mercedesbenz.sechub.xraywrapper.cli;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIResponse;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;
import com.mercedesbenz.sechub.xraywrapper.reportgenerator.XrayReportReader;

public class XrayClientArtifactoryManager {

    private String baseUrl;

    private String repository;

    private String reportfiles;

    private XrayDockerImage image;

    public XrayClientArtifactoryManager(XrayConfiguration xrayConfiguration, XrayDockerImage image) {
        this.baseUrl = xrayConfiguration.getArtifactory();
        this.repository = xrayConfiguration.getRegister();
        this.reportfiles = xrayConfiguration.getReport_filename();
        this.image = image;

    }

    public void start() throws IOException {
        // performs all necessary API calls to get reports

        // get xray version from artifactory
        String xray_version = getXrayVersion();
        System.out.println("XRAY Version: " + xray_version);

        // check if artifact is uploaded
        boolean isUploaded = checkArtifactoryUpload();
        if (!isUploaded) {
            // todo: retry x-times
            System.out.println("Error: Component could not be found, was upload correct?");
            System.exit(0);
        } else {
            System.out.println("Artifact successful uploaded");
        }

        String scanned = getScanStatus();
        String startScan;
        int retries = 0;
        if (scanned.equals("error"))
            System.exit(0);
        /*
         * // Todo: if artifact is uploaded scan is started automatic but delay in
         * response --> error when staring scan again if (scanned.equals("not scanned"))
         * { // start scan if not started yet startScan = startScanArtifact(); if
         * (startScan.equals("Scan of artifact is in progress")) scanned =
         * "in progress"; }
         */

        while ((scanned.equals("in progress") || scanned.equals("not scanned")) && retries < 6) {
            System.out.println("Scan in progress...");
            waitSeconds(10);
            retries++;
            // todo: may have dely? and return ist not scanned?
            scanned = getScanStatus();
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
        String filename = reportfiles;
        XrayReportReader reportReader = new XrayReportReader();
        reportReader.readReport(filename);
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
        request = XrayAPIcalls.getScanReports(baseUrl, image, reportfiles);
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

    public static void waitSeconds(int val) {
        try {
            TimeUnit.SECONDS.sleep(val);
        } catch (InterruptedException e) {
            // log.error("Thread interrupted");
        }
    }

}
