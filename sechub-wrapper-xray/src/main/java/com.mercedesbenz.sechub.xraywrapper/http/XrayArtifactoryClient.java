package com.mercedesbenz.sechub.xraywrapper.http;

import java.io.IOException;
import java.net.HttpURLConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

public class XrayArtifactoryClient {

    private XrayArtifact artifact;

    private XrayConfiguration xrayConfiguration;

    public XrayArtifactoryClient(XrayArtifact artifact, XrayConfiguration xrayConfiguration) {
        this.artifact = artifact;
        this.xrayConfiguration = xrayConfiguration;
    }

    public String getXrayVersion() throws IOException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildGetXrayVersion(xrayConfiguration.getArtifactory());
        XrayAPIResponse response = send(request);
        JsonNode node = getBodyAsNode(response.getBody());
        return node.get("xray_version").asText();
    }

    public boolean checkArtifactoryUpload() throws IOException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildCheckArtifactUpload(xrayConfiguration.getArtifactory(), artifact,
                xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        return !(isErrorResponse(response));
    }

    public String getScanStatus() throws IOException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildGetScanStatus(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (!isErrorResponse(response)) {
            JsonNode node = getBodyAsNode(response.getBody());
            return node.get("status").asText();
        }
        return "error";
    }

    public boolean requestScanReports() throws IOException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildGetScanReports(xrayConfiguration.getArtifactory(), artifact);
        XrayAPIResponse response = send(request);
        return !isErrorResponse(response);
    }

    public String startScanArtifact() throws IOException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildScanArtifact(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (!isErrorResponse(response)) {
            JsonNode node = getBodyAsNode(response.getBody());
            return node.get("info").asText();
        }
        return "";
    }

    private JsonNode getBodyAsNode(String body) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(body);
    }

    public XrayAPIResponse send(XrayAPIRequest request) throws IOException {
        HttpURLConnection con = XrayHttpRequestExecutor.setUpGetConnection(request);
        return XrayHttpResponseBuilder.getHttpResponseFromConnection(con, xrayConfiguration.getZip_directory());
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

}
