// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.api;

import static com.mercedesbenz.sechub.wrapper.xray.api.XrayAPIConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperExitCode;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

class XrayWrapperArtifactoryClientTest {
    XrayAPIResponse response;

    Map<String, List<String>> headers;

    @BeforeEach
    void beforeEach() {
        headers = new HashMap<>();
    }

    @Test
    void getXrayVersion_return_version() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();
            String jsonString = "{\"" + XRAY_VERSION + "\": \"123\"}";
            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            String version = clientToTest.requestXrayVersion();

            /* test */
            assertEquals("123", version);
        }
    }

    @Test
    void getXrayVersion_invalid_json_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            String jsonString = "{\"" + XRAY_VERSION + "\": \"1";
            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.requestXrayVersion());

            /* test */
            assertEquals("Cannot parse provided string into JSON", exception.getMessage());
            assertEquals(XrayWrapperExitCode.INVALID_JSON, exception.getExitCode());
        }
    }

    @Test
    void checkArtifactoryUpload_returns_true() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            clientToTest.assertArtifactoryUploadSuccess();

            /* test */
            verify(clientToTest.xrayAPIResponseFactory, times(1)).createHttpResponseFromConnection(any(), any());
        }
    }

    @Test
    void checkArtifactoryUpload_error_response_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            XrayAPIResponse response = XrayAPIResponse.Builder.builder().httpStatusCode(500).headers(headers).build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.assertArtifactoryUploadSuccess());

            /* test */
            assertEquals("Artifact was not uploaded to artifactory, status code: 500, serverMessage: null, errorBody: ", exception.getMessage());
            assertEquals(XrayWrapperExitCode.ARTIFACTORY_ERROR_RESPONSE, exception.getExitCode());
        }
    }

    @Test
    void getScanStatus_return_status_scanned() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            String jsonString = "{\"" + XRAY_STATUS + "\": \"scanned\"}";
            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            String status = clientToTest.getScanStatus().getStatusValue();

            /* test */
            assertEquals("scanned", status);
        }
    }

    @Test
    void getScanStatus_return_unexpected_status_scanned() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            String jsonString = "{\"" + XRAY_STATUS + "\": \"unexpected\"}";
            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.getScanStatus().getStatusValue());

            /* test */
            assertEquals("Received unexpected scan status", exception.getMessage());
        }
    }

    @Test
    void getScanStatus_invalid_json_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            String jsonString = "{\"" + XRAY_VERSION + "\": \"1";
            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute + test */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.getScanStatus());

            /* test */
            assertEquals("Cannot parse provided string into JSON", exception.getMessage());
            assertEquals(XrayWrapperExitCode.INVALID_JSON, exception.getExitCode());
        }
    }

    @Test
    void requestScanReports_return_true() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            assertTrue(clientToTest.requestScanReports());

            /* test */
            verify(clientToTest.xrayAPIResponseFactory, times(1)).createHttpResponseFromConnection(any(), any());
        }
    }

    @Test
    void requestScanReports_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            response = XrayAPIResponse.Builder.builder().httpStatusCode(500).headers(headers).addResponseBody("").addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.requestScanReports());

            /* test */
            assertEquals("Could not get report from artifactory, status code: 500, serverMessage: null, errorBody: ", exception.getMessage());
            assertEquals(XrayWrapperExitCode.ARTIFACTORY_ERROR_RESPONSE, exception.getExitCode());
        }
    }

    @Test
    void startScanArtifact_return_valid() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            String jsonString = "{\"" + XRAY_INFO + "\": \"Scan of artifact is in progress\"}";
            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            clientToTest.startArtifactScan();

            /* test */
            verify(clientToTest.xrayAPIResponseFactory, times(1)).createHttpResponseFromConnection(any(), any());
        }
    }

    @Test
    void startScanArtifact_invalid_json_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            String jsonString = "{\"" + XRAY_VERSION + "\": \"1";
            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody(jsonString).addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.startArtifactScan());

            /* test */
            assertEquals("Cannot parse provided string into JSON", exception.getMessage());
            assertEquals(XrayWrapperExitCode.INVALID_JSON, exception.getExitCode());
        }
    }

    @Test
    void deleteArtifact_valid() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            // method does not return value
            clientToTest.deleteArtifact();

            /* test */
            verify(clientToTest.xrayAPIResponseFactory, times(1)).createHttpResponseFromConnection(any(), any());
        }
    }

    @Test
    void deleteArtifact_error_message_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            response = XrayAPIResponse.Builder.builder().httpStatusCode(401).headers(headers).build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.deleteArtifact());

            /* test */
            assertEquals("Could not delete artifact from repo, status code: 401, serverMessage: null, errorBody: ", exception.getMessage());
            assertEquals(XrayWrapperExitCode.ARTIFACTORY_ERROR_RESPONSE, exception.getExitCode());
        }
    }

    @Test
    void deleteUploads_valid() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            response = XrayAPIResponse.Builder.builder().httpStatusCode(200).headers(headers).addResponseBody("").addResponseMessage("Message").build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            // method does not return value
            clientToTest.deleteUploads();

            /* test */
            verify(clientToTest.xrayAPIResponseFactory, times(1)).createHttpResponseFromConnection(any(), any());
        }
    }

    @Test
    void deleteUploads_error_response_throws_xrayWrapperException() throws XrayWrapperException {
        /* prepare */
        try (MockedConstruction<XrayAPIHTTPUrlConnectionFactory> mockConstruction = mockConstruction(XrayAPIHTTPUrlConnectionFactory.class)) {
            XrayAPIArtifactoryClient clientToTest = createClient();

            response = XrayAPIResponse.Builder.builder().httpStatusCode(401).headers(headers).build();
            when(clientToTest.xrayAPIResponseFactory.createHttpResponseFromConnection(any(), any())).thenReturn(response);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientToTest.deleteUploads());

            /* test */
            assertEquals("Could not delete _uploads from artifactory, status code: 401, serverMessage: null, errorBody: ", exception.getMessage());
            assertEquals(XrayWrapperExitCode.ARTIFACTORY_ERROR_RESPONSE, exception.getExitCode());
        }
    }

    private XrayAPIArtifactoryClient createClient() {
        XrayWrapperArtifact artifact = new XrayWrapperArtifact("name", "sha256", "tag", XrayWrapperScanTypes.DOCKER);
        String url = "http://notmalformed-url-example.com";
        String registry = "example";
        String zipDir = "zipDirectory";
        String report = "report";
        XrayWrapperConfiguration configuration = XrayWrapperConfiguration.Builder.builder().artifactory(url).registry(registry).zipDirectory(zipDir)
                .xrayPdsReport(report).build();
        XrayAPIArtifactoryClient client = new XrayAPIArtifactoryClient(artifact, configuration);
        client.xrayAPIResponseFactory = mock(XrayAPIResponseFactory.class);
        return client;
    }
}