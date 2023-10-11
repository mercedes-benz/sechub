package com.mercedesbenz.sechub.xraywrapper.api;

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
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayWrapperConfiguration;

class XrayWrapperArtifactoryClientTest {

    XrayAPIArtifactoryClient xrayAPIArtifactoryClient;

    XrayAPIArtifactoryClient xrayAPIArtifactoryClientSpy;

    XrayAPIResponse response;

    @BeforeEach
    public void beforeEach() {
        XrayWrapperArtifact artifact = mock(XrayWrapperArtifact.class);
        XrayWrapperConfiguration configuration = mock(XrayWrapperConfiguration.class);
        xrayAPIArtifactoryClient = new XrayAPIArtifactoryClient(artifact, configuration);
        xrayAPIArtifactoryClientSpy = Mockito.spy(xrayAPIArtifactoryClient);
        response = mock(XrayAPIResponse.class);
    }

    @Test
    public void test_getXrayVersion() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"xray_version\": \"123\"}";
        response.setBody(jsonString);
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayAPIArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String version = xrayAPIArtifactoryClientSpy.getXrayVersion();

        /* test */
        assertEquals("123", version);
    }

    @Test
    public void test_getXrayVersion_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.getXrayVersion());
    }

    @Test
    public void test_checkArtifactoryUpload() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.checkArtifactoryUpload());
    }

    @Test
    public void test_checkArtifactoryUpload_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.checkArtifactoryUpload());
    }

    @Test
    public void test_getScanStatus() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"status\": \"scanned\"}";
        response.setBody(jsonString);
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayAPIArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String status = xrayAPIArtifactoryClientSpy.getScanStatus();

        /* test */
        assertEquals("scanned", status);
    }

    @Test
    public void test_getScanStatus_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.getScanStatus());
    }

    @Test
    public void test_requestScanReports() {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        assertTrue(xrayAPIArtifactoryClientSpy.requestScanReports());
    }

    @Test
    public void test_requestScanReports_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.requestScanReports());
    }

    @Test
    public void test_startScanArtifact() throws JsonProcessingException {
        /* prepare */
        ObjectMapper mapper = new ObjectMapper();
        String jsonString = "{\"info\": \"info_string\"}";
        response.setBody(jsonString);
        JsonNode node = mapper.readTree(jsonString);
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());
        Mockito.doReturn(node).when(xrayAPIArtifactoryClientSpy).getBodyAsNode(any());

        /* execute */
        String status = xrayAPIArtifactoryClientSpy.startScanArtifact();

        /* test */
        assertEquals("info_string", status);
    }

    @Test
    public void test_startScanArtifact_xrayWrapperException() {
        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.startScanArtifact());
    }

    @Test
    public void test_deleteArtifact() throws JsonProcessingException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        xrayAPIArtifactoryClientSpy.deleteArtifact();
    }

    @Test
    public void test_deleteArtifact_xrayWrapperException() {
        /* prepare */
        response.setStatus_code(401);

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.deleteArtifact());
    }

    @Test
    public void test_deleteUploads() throws JsonProcessingException {
        /* prepare */
        Mockito.doReturn(response).when(xrayAPIArtifactoryClientSpy).send(any());

        /* execute + test */
        xrayAPIArtifactoryClientSpy.deleteUploads();
    }

    @Test
    public void test_ddeleteUploads_xrayWrapperException() {
        /* prepare */
        response.setStatus_code(401);

        /* execute + test */
        assertThrows(XrayWrapperRuntimeException.class, () -> xrayAPIArtifactoryClient.deleteUploads());
    }

}