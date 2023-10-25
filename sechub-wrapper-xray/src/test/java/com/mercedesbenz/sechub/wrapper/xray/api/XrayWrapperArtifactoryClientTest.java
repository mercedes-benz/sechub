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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.wrapper.xray.cli.XrayWrapperRuntimeException;
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
                .create("http://notmalformed-url-example.com", "example", "zipDirectory", "report").build();
        xrayAPIArtifactoryClient = new XrayAPIArtifactoryClient(artifact, configuration);
        xrayAPIArtifactoryClientSpy = Mockito.spy(xrayAPIArtifactoryClient);
        response = mock(XrayAPIResponse.class);
        headers = new java.util.HashMap<>(Collections.emptyMap());
    }

    @Test
    void getXrayVersion_return_version() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"xray_version\": \"123\"}";
        response = XrayAPIResponse.Builder.create(200, headers).setBody(jsonString).build();
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayAPIArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String version = xrayAPIArtifactoryClientSpy.getXrayVersion();

        /* test */
        assertEquals("123", version);
    }

    @Test
    void getXrayVersion_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.getXrayVersion());
    }

    @Test
    void checkArtifactoryUpload_return_true() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.checkArtifactoryUpload());
    }

    @Test
    void checkArtifactoryUpload_throws_xrayWrapperException() {
        /* prepare */
        XrayAPIResponse response = XrayAPIResponse.Builder.create(500, headers).build();
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.checkArtifactoryUpload());
    }

    @Test
    void getScanStatus_return_status_scanned() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"status\": \"scanned\"}";
        response = XrayAPIResponse.Builder.create(200, headers).setBody(jsonString).build();
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayAPIArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String status = xrayAPIArtifactoryClientSpy.getScanStatus();

        /* test */
        assertEquals("scanned", status);
    }

    @Test
    void getScanStatus_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.getScanStatus());
    }

    @Test
    void requestScanReports_return_true() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.requestScanReports());
    }

    @Test
    void requestScanReports_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.requestScanReports());
    }

    @Test
    void startScanArtifact_return_valid() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"info\": \"info_string\"}";
        response = XrayAPIResponse.Builder.create(200, headers).setBody(jsonString).build();
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayAPIArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String status = xrayAPIArtifactoryClientSpy.startScanArtifact();

        /* test */
        assertEquals("info_string", status);
    }

    @Test
    void startScanArtifact_throws_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.startScanArtifact());
    }

    @Test
    void deleteArtifact_valid() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        // method does not return value
        xrayAPIArtifactoryClientSpy.deleteArtifact();
    }

    @Test
    void deleteArtifact_throws_xrayWrapperException() {
        /* prepare */
        response = XrayAPIResponse.Builder.create(401, headers).build();

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.deleteArtifact());
    }

    @Test
    void deleteUploads_valid() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        // method does not return value
        xrayAPIArtifactoryClientSpy.deleteUploads();
    }

    @Test
    void deleteUploads_throws_xrayWrapperException() {
        /* prepare */
        response = XrayAPIResponse.Builder.create(401, headers).build();

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.deleteUploads());
    }

}