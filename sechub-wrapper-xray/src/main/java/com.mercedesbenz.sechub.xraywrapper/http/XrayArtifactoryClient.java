package com.mercedesbenz.sechub.xraywrapper.http;

import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

public class XrayArtifactoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(XrayArtifactoryClient.class);

    private XrayArtifact artifact;

    private XrayConfiguration xrayConfiguration;

    public XrayArtifactoryClient(XrayArtifact artifact, XrayConfiguration xrayConfiguration) {
        this.artifact = artifact;
        this.xrayConfiguration = xrayConfiguration;
    }

    public String getXrayVersion() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildGetXrayVersion(xrayConfiguration.getArtifactory());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            throw new XrayWrapperRuntimeException("Artifactory not reachable", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        } else {
            JsonNode node = getBodyAsNode(response.getBody());
            return node.get("xray_version").asText();
        }
    }

    public boolean checkArtifactoryUpload() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildCheckArtifactUpload(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Error: artifact was not uploaded to artifactory");
            throw new XrayWrapperRuntimeException("Artifact not found in artifactory", XrayWrapperExitCode.ARTIFACT_NOT_FOUND);
        }
        return true;
    }

    public String getScanStatus() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildGetScanStatus(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Error: scan status could not be retrieved");
            throw new XrayWrapperRuntimeException("Scan status not available", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        }
        JsonNode node = getBodyAsNode(response.getBody());
        return node.get("status").asText();
    }

    public boolean requestScanReports() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildGetScanReports(xrayConfiguration.getArtifactory(), artifact);
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Could not get report from artifactory");
            throw new XrayWrapperRuntimeException("Could not get reports from artifactory", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        }
        return true;
    }

    public String startScanArtifact() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayHttpRequestBuilder.buildScanArtifact(xrayConfiguration.getArtifactory(), artifact, xrayConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Could not start external Xray scan");
            throw new XrayWrapperRuntimeException("Could not start external Xray scan", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        }
        JsonNode node = getBodyAsNode(response.getBody());
        return node.get("info").asText();
    }

    public JsonNode getBodyAsNode(String body) throws XrayWrapperRuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new XrayWrapperRuntimeException("Error: can not read file as json tree", e, XrayWrapperExitCode.JSON_NOT_PROCESSABLE);
        }
    }

    public XrayAPIResponse send(XrayAPIRequest request) throws XrayWrapperRuntimeException {
        HttpURLConnection con = XrayHttpRequestExecutor.setUpGetConnection(request);
        return XrayHttpResponseBuilder.getHttpResponseFromConnection(con, xrayConfiguration.getZip_directory());
    }

    private boolean isErrorResponse(XrayAPIResponse response) {
        int statusCode = response.getStatus_code();
        if (statusCode > 299) {
            LOG.error("Error: received Error Message from artifactory: {}", statusCode);
            LOG.error("Response Headers: {}", response.getHeaders().toString());
            LOG.error("Response Body: {}", response.getBody());
            return true;
        }
        LOG.info("Response Code: {}", statusCode);
        LOG.info("Response Headers: {}", response.getHeaders().toString());
        LOG.info("Response Body: {}", response.getBody());
        return false;
    }

}
