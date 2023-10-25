package com.mercedesbenz.sechub.wrapper.xray.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.xray.api.XrayAPIArtifactoryClient;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

class XrayWrapperArtifactoryClientControllerTest {

    XrayWrapperArtifactoryClientController controller;

    XrayWrapperConfiguration configuration;

    XrayWrapperArtifact artifact;

    @BeforeEach
    void beforeEach() {
        configuration = mock(XrayWrapperConfiguration.class);
        artifact = mock(XrayWrapperArtifact.class);
    }

    @Test
    void waitForScansToFinishAndDownloadReport_valid_execution_without_report_transformation() throws XrayWrapperRuntimeException {
        /* test + execute */
        try (MockedConstruction<XrayAPIArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayAPIArtifactoryClient.class, (mock, context) -> {
            when(mock.getXrayVersion()).thenReturn("mocked-version");
            when(mock.checkArtifactoryUpload()).thenReturn(true);
            when(mock.getScanStatus()).thenReturn("scanned");
            when(mock.requestScanReports()).thenReturn(false);
        })) {
            controller = new XrayWrapperArtifactoryClientController(configuration, artifact);
            controller.waitForScansToFinishAndDownloadReport();
        }
    }

    @Test
    void waitForScansToFinishAndDownloadReport_throws_XrayWrapperRuntimeException() {
        /* prepare */
        controller = new XrayWrapperArtifactoryClientController(configuration, artifact);

        /* test + execute */
        assertThrows(XrayWrapperRuntimeException.class, () -> controller.waitForScansToFinishAndDownloadReport());
    }
}