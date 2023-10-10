package com.mercedesbenz.sechub.xraywrapper.http;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mercedesbenz.sechub.xraywrapper.cli.XrayWrapperRuntimeException;
import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

class XrayArtifactoryClientTest {

    XrayArtifactoryClient xrayArtifactoryClient;

    XrayArtifactoryClient xrayArtifactoryClientSpy;

    XrayAPIResponse response;

    @BeforeEach
    public void beforeEach() {
        XrayArtifact artifact = mock(XrayArtifact.class);
        XrayConfiguration configuration = mock(XrayConfiguration.class);
        xrayArtifactoryClient = new XrayArtifactoryClient(artifact, configuration);
        xrayArtifactoryClientSpy = Mockito.spy(xrayArtifactoryClient);
        response = mock(XrayAPIResponse.class);
    }

    @Test
    public void test_getXrayVersion() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"xray_version\": \"123\"}";
        response.setBody(jsonString);
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String version = xrayArtifactoryClientSpy.getXrayVersion();

        /* test */
        assertEquals("123", version);
    }

    @Test
    public void test_getXrayVersion_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayArtifactoryClient.getXrayVersion());
    }

    @Test
    public void test_checkArtifactoryUpload() {
        /* prepare */
        Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayArtifactoryClientSpy.checkArtifactoryUpload());
    }

    @Test
    public void test_checkArtifactoryUpload_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayArtifactoryClient.checkArtifactoryUpload());
    }

    @Test
    public void test_getScanStatus() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"status\": \"scanned\"}";
        response.setBody(jsonString);
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String status = xrayArtifactoryClientSpy.getScanStatus();

        /* test */
        assertEquals("scanned", status);
    }

    @Test
    public void test_getScanStatus_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayArtifactoryClient.getScanStatus());
    }

    @Test
    public void test_requestScanReports() {
        /* prepare */
        Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayArtifactoryClientSpy.requestScanReports());
    }

    @Test
    public void test_requestScanReports_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayArtifactoryClient.requestScanReports());
    }

    @Test
    public void test_startScanArtifact() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"info\": \"info_string\"}";
        response.setBody(jsonString);
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String status = xrayArtifactoryClientSpy.startScanArtifact();

        /* test */
        assertEquals("info_string", status);
    }

    @Test
    public void test_startScanArtifact_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayArtifactoryClient.startScanArtifact());
    }

    @Test
    public void test_deleteArtifact() throws JsonProcessingException {
        /* prepare */
        Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());

        /* execute + test */
        xrayArtifactoryClientSpy.deleteArtifact();
    }

    @Test
    public void test_deleteArtifact_xrayWrapperException() {
        /* prepare */
        response.setStatus_code(401);

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayArtifactoryClient.deleteArtifact());
    }

    @Test
    public void test_deleteUploads() throws JsonProcessingException {
        /* prepare */
        Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());

        /* execute + test */
        xrayArtifactoryClientSpy.deleteUploads();
    }

    @Test
    public void test_ddeleteUploads_xrayWrapperException() {
        /* prepare */
        response.setStatus_code(401);

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayArtifactoryClient.deleteUploads());
    }

}