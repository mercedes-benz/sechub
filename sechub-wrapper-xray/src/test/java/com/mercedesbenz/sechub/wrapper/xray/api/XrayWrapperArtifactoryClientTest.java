package com.mercedesbenz.sechub.wrapper.xray.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

class XrayWrapperArtifactoryClientTest {
    XrayAPIArtifactoryClient clientToTest;

    XrayAPIResponse response;

    Map<String, List<String>> headers;

    @BeforeAll
    static void beforeAll() {
        mockConstruction(XrayAPIHTTPUrlConnectionFactory.class);
    }

    @BeforeEach
    void beforeEach() {
        XrayWrapperArtifact artifact = new XrayWrapperArtifact("name", "sha256", "tag", XrayWrapperScanTypes.DOCKER);
        String url = "http://notmalformed-url-example.com";
        String registry = "example";
        String zipDir = "zipDirectory";
        String report = "report";
        XrayWrapperConfiguration configuration = XrayWrapperConfiguration.Builder.builder().artifactory(url).registry(registry).zipDirectory(zipDir)
                .xrayPdsReport(report).build();

        clientToTest = new XrayAPIArtifactoryClient(artifact, configuration);
        clientToTest.xrayAPIResponseFactory = mock(XrayAPIResponseFactory.class);
        headers = new java.util.HashMap<>(Collections.emptyMap());
    }

    @Test
    void getXrayVersion_return_version() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"xray_version\": \"123\"}";
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute */
        String version = clientToTest.requestXrayVersion();

        /* test */
        assertEquals("123", version);
    }

    @Test
    void getXrayVersion_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"xray_version\": \"1";
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> clientToTest.requestXrayVersion());
    }

    @Test
    void checkArtifactoryUpload_return_true() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertTrue(clientToTest.checkArtifactoryUploadSuccess());
    }

    @Test
    void checkArtifactoryUpload_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        XrayAPIResponse response = XrayAPIResponse.Builder.builder().statusCode(500).headers(headers).build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> clientToTest.checkArtifactoryUploadSuccess());
    }

    @Test
    void getScanStatus_return_status_scanned() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"status\": \"scanned\"}";
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute */
        String status = clientToTest.getScanStatus().getStatusValue();

        /* test */
        assertEquals("scanned", status);
    }

    @Test
    void getScanStatus_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"xray_version\": \"1";
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> clientToTest.getScanStatus());
    }

    @Test
    void requestScanReports_return_true() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertTrue(clientToTest.requestScanReports());
    }

    @Test
    void requestScanReports_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder().statusCode(500).headers(headers).addResponseBody("").addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> clientToTest.requestScanReports());
    }

    @Test
    void startScanArtifact_return_valid() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"info\": \"Scan of artifact is in progress\"}";
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        clientToTest.startArtifactScan();
    }

    @Test
    void startScanArtifact_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        String jsonString = "{\"xray_version\": \"1";
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> clientToTest.startArtifactScan());
    }

    @Test
    void deleteArtifact_valid() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        // method does not return value
        clientToTest.deleteArtifact();
    }

    @Test
    void deleteArtifact_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder().statusCode(401).headers(headers).build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> clientToTest.deleteArtifact());
    }

    @Test
    void deleteUploads_valid() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder().statusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        // method does not return value
        clientToTest.deleteUploads();
    }

    @Test
    void deleteUploads_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        response = XrayAPIResponse.Builder.builder().statusCode(401).headers(headers).build();
        when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

        /* execute + test */
        assertThrows(XrayWrapperException.class, () -> clientToTest.deleteUploads());
    }

}