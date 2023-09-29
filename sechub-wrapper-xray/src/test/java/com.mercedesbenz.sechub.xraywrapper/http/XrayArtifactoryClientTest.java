package com.mercedesbenz.sechub.xraywrapper.http;

import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;

class XrayArtifactoryClientTest {

    XrayArtifactoryClient xrayArtifactoryClient;

    @BeforeEach
    public void beforeEach() {
        XrayArtifact artifact = mock(XrayArtifact.class);
        XrayConfiguration configuration = mock(XrayConfiguration.class);
        xrayArtifactoryClient = new XrayArtifactoryClient(artifact, configuration);
        // MockedConstruction<XrayAPIRequest> mockedConstruction =
        // Mockito.mockConstruction(XrayAPIRequest.class);

    }

    @Test
    public void test_getXrayVersion() throws JsonProcessingException {
        /*
         * TODO XrayAPIResponse response = mock(XrayAPIResponse.class); ObjectMapper
         * mapper = new ObjectMapper(); String jsonString = "{\"car\": \"red\"}";
         * JsonNode node = mapper.readTree(jsonString); response.setBody(jsonString);
         * XrayArtifactoryClient xrayArtifactoryClientSpy =
         * Mockito.spy(xrayArtifactoryClient);
         * Mockito.doReturn(response).when(xrayArtifactoryClientSpy).send(any());
         * Mockito.doReturn(node).when(xrayArtifactoryClientSpy).getBodyAsNode(anyString
         * ());
         */
        /* execute + test */
        // xrayArtifactoryClientSpy.getXrayVersion();

    }

}