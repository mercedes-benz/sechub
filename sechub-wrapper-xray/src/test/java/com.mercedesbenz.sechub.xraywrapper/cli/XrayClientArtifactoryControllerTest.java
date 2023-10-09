package com.mercedesbenz.sechub.xraywrapper.cli;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.xraywrapper.config.XrayArtifact;
import com.mercedesbenz.sechub.xraywrapper.config.XrayConfiguration;
import com.mercedesbenz.sechub.xraywrapper.http.XrayArtifactoryClient;

class XrayClientArtifactoryControllerTest {

    XrayClientArtifactoryController controller;

    XrayConfiguration configuration;

    XrayArtifact artifact;

    @BeforeEach
    public void beforeEach() {
        configuration = mock(XrayConfiguration.class);
        artifact = mock(XrayArtifact.class);
    }

    @Test
    public void test_waitForScansToFinishAndDownloadReport() throws XrayWrapperRuntimeException {
        /* test + execute */
        try (MockedConstruction<XrayArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayArtifactoryClient.class, (mock, context) -> {
            when(mock.getXrayVersion()).thenReturn("mocked-version");
            when(mock.checkArtifactoryUpload()).thenReturn(true);
            when(mock.getScanStatus()).thenReturn("scanned");
            when(mock.requestScanReports()).thenReturn(false);
        })) {
            controller = new XrayClientArtifactoryController(configuration, artifact);
            controller.waitForScansToFinishAndDownloadReport();
        }
    }
}