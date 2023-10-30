package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

class XrayWrapperArtifactoryClientTest {

    XrayAPIArtifactoryClient xrayAPIArtifactoryClient;

    XrayAPIArtifactoryClient xrayAPIArtifactoryClientSpy;

    XrayAPIResponse response;

    Map<String, List<String>> headers;

    @BeforeEach
    void beforeEach() {
        XrayWrapperArtifact artifact = new XrayWrapperArtifact("name", "sha256", "tag", XrayWrapperScanTypes.DOCKER);
        XrayWrapperConfiguration configuration = XrayWrapperConfiguration.Builder
                .builder("http://notmalformed-url-example.com", "example", "zipDirectory", "report").build();
        xrayAPIArtifactoryClient = new XrayAPIArtifactoryClient(artifact, configuration);
        xrayAPIArtifactoryClientSpy = Mockito.spy(xrayAPIArtifactoryClient);
        response = mock(XrayAPIResponse.class);
        headers = new java.util.HashMap<>(Collections.emptyMap());
    }

    @Test
    void getXrayVersion_return_version() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"xray_version\": \"123\"}";
        response = XrayAPIResponse.Builder.builder(200, headers).addResponseBody(jsonString).build();
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute */
        String version = xrayAPIArtifactoryClientSpy.getXrayVersion();

        /* test */
        assertEquals("123", version);
    }

    @Test
    void getXrayVersion_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIArtifactoryClient.getXrayVersion());
    }

    @Test
    void checkArtifactoryUpload_return_true() throws XrayWrapperException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.checkArtifactoryUploadSuccess());
    }

    @Test
    void checkArtifactoryUpload_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        XrayAPIResponse response = XrayAPIResponse.Builder.builder(500, headers).build();
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIArtifactoryClient.checkArtifactoryUploadSuccess());
    }

    @Test
    void getScanStatus_return_status_scanned() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"status\": \"scanned\"}";
        response = XrayAPIResponse.Builder.builder(200, headers).addResponseBody(jsonString).build();
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute */
        String status = xrayAPIArtifactoryClientSpy.getScanStatus().getStatus();

        /* test */
        assertEquals("scanned", status);
    }

    @Test
    void getScanStatus_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIArtifactoryClient.getScanStatus());
    }

    @Test
    void requestScanReports_return_true() throws XrayWrapperException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.requestScanReports());
    }

    @Test
    void requestScanReports_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIArtifactoryClient.requestScanReports());
    }

    @Test
    void startScanArtifact_return_valid() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"info\": \"Scan of artifact is in progress\"}";
        response = XrayAPIResponse.Builder.builder(200, headers).addResponseBody(jsonString).build();
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        xrayAPIArtifactoryClientSpy.startArtifactScan();
    }

    @Test
    void startScanArtifact_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIArtifactoryClient.startArtifactScan());
    }

    @Test
    void deleteArtifact_valid() throws XrayWrapperException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        // method does not return value
        xrayAPIArtifactoryClientSpy.deleteArtifact();
    }

    @Test
    void deleteArtifact_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder(401, headers).build();

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIArtifactoryClient.deleteArtifact());
    }

    @Test
    void deleteUploads_valid() throws XrayWrapperException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        // method does not return value
        xrayAPIArtifactoryClientSpy.deleteUploads();
    }

    @Test
    void deleteUploads_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder(401, headers).build();

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> xrayAPIArtifactoryClient.deleteUploads());
    }

}