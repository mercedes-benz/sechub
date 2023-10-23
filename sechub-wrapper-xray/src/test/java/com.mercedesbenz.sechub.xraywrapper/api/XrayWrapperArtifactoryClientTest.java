package com.mercedesbenz.sechub.xraywrapper.api;

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
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperScanTypes;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperConfiguration;

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
    void test_getXrayVersion() throws JsonProcessingException {
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
    void test_getXrayVersion_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.getXrayVersion());
    }

    @Test
    void test_checkArtifactoryUpload() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.checkArtifactoryUpload());
    }

    @Test
    void test_checkArtifactoryUpload_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.checkArtifactoryUpload());
    }

    @Test
    void test_getScanStatus() throws JsonProcessingException {
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
    void test_getScanStatus_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.getScanStatus());
    }

    @Test
    void test_requestScanReports() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.requestScanReports());
    }

    @Test
    void test_requestScanReports_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.requestScanReports());
    }

    @Test
    void test_startScanArtifact() throws JsonProcessingException {
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
    void test_startScanArtifact_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.startScanArtifact());
    }

    @Test
    void test_deleteArtifact() throws JsonProcessingException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.deleteArtifact());
    }

    @Test
    void test_deleteArtifact_xrayWrapperException() {
        /* prepare */
        response = XrayAPIResponse.Builder.create(401, headers).build();

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.deleteArtifact());
    }

    @Test
    void test_deleteUploads() throws JsonProcessingException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.deleteUploads());
    }

    @Test
    void test_deleteUploads_xrayWrapperException() {
        /* prepare */
        response = XrayAPIResponse.Builder.create(401, headers).build();

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.deleteUploads());
    }

}