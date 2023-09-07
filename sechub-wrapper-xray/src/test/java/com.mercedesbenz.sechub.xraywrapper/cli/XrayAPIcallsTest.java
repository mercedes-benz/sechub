package com.mercedesbenz.sechub.xraywrapper.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.xraywrapper.helper.XrayAPIRequest;
import com.mercedesbenz.sechub.xraywrapper.helper.XrayDockerImage;

class XrayAPIcallsTest {

    // prepare
    String baseUrl = "http://baseurl";
    String repository = "my_repository";
    String filename = "filename";
    XrayAPIRequest request;
    XrayDockerImage image = new XrayDockerImage("name", "1.0", "sha256");

    @Test
    public void testGetXrayVersion() {
        // execute
        request = XrayAPIcalls.getXrayVersion(baseUrl);

        // assert
        assertEquals(baseUrl + "/xray/api/v1/system/version", request.getBaseUrl());
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
    }

    @Test
    public void testCheckArtifactUpload() {
        // prepare
        String url = baseUrl + "/artifactory/api/storage/" + repository + "/" + image.getDocker_name() + "/" + image.getDocker_tag() + "/manifest.json";

        // execute
        request = XrayAPIcalls.checkArtifactUpload(baseUrl, image, repository);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.GET, request.getRequestMethodEnum());
        assertEquals(url, request.getBaseUrl());
    }

    @Test
    public void testScanArtifact() {
        // prepare
        String url = baseUrl + "/xray/api/v1/scanArtifact";

        // execute
        request = XrayAPIcalls.scanArtifact(baseUrl, image, repository);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertTrue(request.getData().contains(repository));
        assertEquals(url, request.getBaseUrl());
    }

    @Test
    public void testGetScanStatus() {
        // prepare
        String url = baseUrl + "/xray/api/v1/scan/status/artifact";

        // execute
        request = XrayAPIcalls.getScanStatus(baseUrl, image, repository);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertTrue(request.getData().contains(repository));
        assertEquals(url, request.getBaseUrl());
    }

    @Test
    public void testGetScanReports() {
        // prepare
        String url = baseUrl + "/xray/api/v1/component/exportDetails";

        // execute
        request = XrayAPIcalls.getScanReports(baseUrl, image, filename);

        // assert
        assertEquals(XrayAPIRequest.RequestMethodEnum.POST, request.getRequestMethodEnum());
        assertTrue(request.getData().contains(image.getDocker_name()));
        assertEquals(url, request.getBaseUrl());
    }

}