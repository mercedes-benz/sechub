package com.mercedesbenz.sechub.xraywrapper.api;

import java.net.HttpURLConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperConfiguration;

public class XrayAPIArtifactoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(XrayAPIArtifactoryClient.class);

    private XrayWrapperArtifact artifact;

    private XrayWrapperConfiguration xrayWrapperConfiguration;

    public XrayAPIArtifactoryClient(XrayWrapperArtifact artifact, XrayWrapperConfiguration xrayWrapperConfiguration) {
        this.artifact = artifact;
        this.xrayWrapperConfiguration = xrayWrapperConfiguration;
    }

    public String getXrayVersion() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayAPIRequestBuilder.buildGetXrayVersion(xrayWrapperConfiguration.getArtifactory());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            throw new XrayWrapperRuntimeException("Artifactory not reachable", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        } else {
            JsonNode node = getBodyAsNode(response.getBody());
            return node.get("xray_version").asText();
        }
    }

    public boolean checkArtifactoryUpload() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayAPIRequestBuilder.buildCheckArtifactUpload(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Error: artifact was not uploaded to artifactory");
            throw new XrayWrapperRuntimeException("Artifact not found in artifactory", XrayWrapperExitCode.ARTIFACT_NOT_FOUND);
        }
        return true;
    }

    public String getScanStatus() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayAPIRequestBuilder.buildGetScanStatus(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Error: scan status could not be retrieved");
            throw new XrayWrapperRuntimeException("Scan status not available", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        }
        JsonNode node = getBodyAsNode(response.getBody());
        return node.get("status").asText();
    }

    public boolean requestScanReports() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayAPIRequestBuilder.buildGetScanReports(xrayWrapperConfiguration.getArtifactory(), artifact);
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Could not get report from artifactory");
            throw new XrayWrapperRuntimeException("Could not get reports from artifactory", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        }
        return true;
    }

    public String startScanArtifact() throws XrayWrapperRuntimeException {
        XrayAPIRequest request = XrayAPIRequestBuilder.buildScanArtifact(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Could not start external Xray scan");
            throw new XrayWrapperRuntimeException("Could not start external Xray scan", XrayWrapperExitCode.ARTIFACTORY_NOT_REACHABLE);
        }
        JsonNode node = getBodyAsNode(response.getBody());
        return node.get("info").asText();
    }

    public void deleteArtifact() throws XrayWrapperRuntimeException {
        // Xray deletes empty folders with auto cleanup
        // deletes artifact folder in artifactory
        XrayAPIRequest request = XrayAPIRequestBuilder.buildDeleteArtifact(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Could not delete artifact from artifactory");
            throw new XrayWrapperRuntimeException("Could not delete artifact from artifactory", XrayWrapperExitCode.ARTIFACT_NOT_FOUND);
        }
    }

    public void deleteUploads() throws XrayWrapperRuntimeException {
        // deletes _uploads folder in artifactory
        XrayAPIRequest request = XrayAPIRequestBuilder.buildDeleteUploads(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegister());
        XrayAPIResponse response = send(request);
        if (isErrorResponse(response)) {
            LOG.error("Could not delete _uploads from artifactory");
            throw new XrayWrapperRuntimeException("Could not delete artifact from artifactory", XrayWrapperExitCode.ARTIFACT_NOT_FOUND);
        }
    }

    JsonNode getBodyAsNode(String body) throws XrayWrapperRuntimeException {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readTree(body);
        } catch (JsonProcessingException e) {
            throw new XrayWrapperRuntimeException("Can not read file as json tree", e, XrayWrapperExitCode.JSON_NOT_PROCESSABLE);
        }
    }

    XrayAPIResponse send(XrayAPIRequest request) throws XrayWrapperRuntimeException {
        HttpURLConnection con = XrayAPIRequestExecutor.setUpGetConnection(request);
        return XrayAPIResponseBuilder.getHttpResponseFromConnection(con, xrayWrapperConfiguration.getZip_directory());
    }

    private boolean isErrorResponse(XrayAPIResponse response) {
        int statusCode = response.getStatus_code();
        if (statusCode > 299) {
            LOG.error("Received Error Message from artifactory: {}", statusCode);
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
