// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import static com.mercedesbenz.sechub.wrapper.xray.api.XrayAPIConstants.*;

import java.net.HttpURLConnection;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperJSONConverter;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperArtifactoryClientSupport;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

public class XrayAPIArtifactoryClient {

    private static final Logger LOG = LoggerFactory.getLogger(XrayAPIArtifactoryClient.class);

    private final XrayWrapperArtifact artifact;

    private final XrayWrapperConfiguration xrayWrapperConfiguration;

    XrayAPIHTTPUrlConnectionFactory apiHttpUrlConnectionFactory = new XrayAPIHTTPUrlConnectionFactory();

    XrayAPIResponseFactory xrayAPIResponseFactory = new XrayAPIResponseFactory();

    public XrayAPIArtifactoryClient(XrayWrapperArtifact artifact, XrayWrapperConfiguration xrayWrapperConfiguration) {
        this.artifact = artifact;
        this.xrayWrapperConfiguration = xrayWrapperConfiguration;
    }

    public String requestXrayVersion() throws XrayWrapperException {
        XrayAPIRequest request = XrayAPIRequestFactory.createGetXrayVersionRequest(xrayWrapperConfiguration.getArtifactory());
        XrayAPIResponse response = send(request);

        assertNoError(response, "Could not get Xray Version from Artifactory");
        JsonNode node = XrayWrapperJSONConverter.get().readJSONFromString(response.getBody());
        return node.get(XRAY_VERSION).asText();
    }

    public void assertArtifactoryUploadSuccess() throws XrayWrapperException {
        XrayAPIRequest request = XrayAPIRequestFactory.createCheckArtifactUploadRequest(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegistry());
        XrayAPIResponse response = send(request);

        assertNoError(response, "Artifact was not uploaded to artifactory");
    }

    public XrayWrapperArtifactoryClientSupport.ScanStatus getScanStatus() throws XrayWrapperException {
        XrayAPIRequest request = XrayAPIRequestFactory.createGetScanStatusRequest(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegistry());
        XrayAPIResponse response = send(request);

        assertNoError(response, "Scan status could not be retrieved");
        JsonNode node = XrayWrapperJSONConverter.get().readJSONFromString(response.getBody());
        return XrayWrapperArtifactoryClientSupport.ScanStatus.fromString(node.get(XRAY_STATUS).asText());
    }

    public boolean requestScanReports() throws XrayWrapperException {
        XrayAPIRequest request = XrayAPIRequestFactory.createGetScanReportsRequest(xrayWrapperConfiguration.getArtifactory(), artifact);
        XrayAPIResponse response = send(request);

        assertNoError(response, "Could not get report from artifactory");
        return true;
    }

    public void startArtifactScan() throws XrayWrapperException {
        XrayAPIRequest request = XrayAPIRequestFactory.createScanArtifactRequest(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegistry());
        XrayAPIResponse response = send(request);

        assertNoError(response, "Could not start external Xray scan");
        JsonNode node = XrayWrapperJSONConverter.get().readJSONFromString(response.getBody());
        String result = node.get(XRAY_INFO).asText();
        if (!Objects.equals(result, "Scan of artifact is in progress")) {
            throw new XrayAPIException("Start of scan was not successful: " + result, response.getHttpStatusCode(), response.getResponseMessage(),
                    response.getBody());
        }
    }

    public void deleteArtifact() throws XrayWrapperException {
        // Xray deletes empty folders with auto cleanup
        // deletes artifact folder in artifactory
        XrayAPIRequest request = XrayAPIRequestFactory.createDeleteArtifactRequest(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegistry());
        XrayAPIResponse response = send(request);

        assertNoError(response, "Could not delete artifact from repo");
    }

    public void deleteUploads() throws XrayWrapperException {
        // deletes _uploads folder in artifactory
        // the _uploads folder is created when any artifact is uploaded to the
        // artifactory
        XrayAPIRequest request = XrayAPIRequestFactory.createDeleteUploadsRequest(xrayWrapperConfiguration.getArtifactory(), artifact,
                xrayWrapperConfiguration.getRegistry());
        XrayAPIResponse response = send(request);

        assertNoError(response, "Could not delete _uploads from artifactory");
    }

    private XrayAPIResponse send(XrayAPIRequest request) throws XrayWrapperException {
        HttpURLConnection con = apiHttpUrlConnectionFactory.create(request);
        return xrayAPIResponseFactory.createHttpResponseFromConnection(con, xrayWrapperConfiguration.getZipDirectory());
    }

    private boolean isErrorResponse(XrayAPIResponse response) {
        int statusCode = response.getHttpStatusCode();
        LOG.debug("Response Code: {}", statusCode);
        LOG.debug("Response Message: {}", response.getResponseMessage());
        LOG.debug("Response Headers: {}", response.getHeaders());
        LOG.debug("Response Body: {}", response.getBody());
        return statusCode > 399;
    }

    private void assertNoError(XrayAPIResponse response, String errorMessage) throws XrayWrapperException {
        if (isErrorResponse(response)) {
            throw new XrayAPIException(errorMessage, response.getHttpStatusCode(), response.getResponseMessage(), response.getBody());
        }
    }
}
