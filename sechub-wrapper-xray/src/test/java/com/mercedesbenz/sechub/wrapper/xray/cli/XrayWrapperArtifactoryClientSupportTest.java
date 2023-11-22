package com.mercedesbenz.sechub.wrapper.xray.cli;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.api.XrayAPIArtifactoryClient;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;

class XrayWrapperArtifactoryClientSupportTest {

    XrayWrapperArtifactoryClientSupport clientSupportToTest;

    XrayWrapperConfiguration configuration;

    XrayWrapperArtifact artifact;

    @BeforeEach
    void beforeEach() {
        configuration = mock(XrayWrapperConfiguration.class);
        artifact = mock(XrayWrapperArtifact.class);
    }

    @Test
    void waitForScansToFinishAndDownloadReport_valid_execution_without_report_transformation() throws XrayWrapperException {
        /* test + execute */
        try (MockedConstruction<XrayAPIArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayAPIArtifactoryClient.class, (mock, context) -> {
            when(mock.requestXrayVersion()).thenReturn("mocked-version");
            when(mock.checkArtifactoryUploadSuccess()).thenReturn(true);
            when(mock.getScanStatus()).thenReturn(XrayWrapperArtifactoryClientSupport.ScanStatus.SCANNED);
            when(mock.requestScanReports()).thenReturn(false);
        })) {
            clientSupportToTest = new XrayWrapperArtifactoryClientSupport(configuration, artifact);
            clientSupportToTest.waitForScansToFinishAndDownloadReport();
        }
    }

    @Test
    void waitForScansToFinishAndDownloadReport_artifact_not_uploaded_throws_XrayWrapperException() {
        /* test + execute */
        try (MockedConstruction<XrayAPIArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayAPIArtifactoryClient.class, (mock, context) -> {
            when(mock.requestXrayVersion()).thenReturn("mocked-version");
            when(mock.checkArtifactoryUploadSuccess()).thenReturn(true);
            when(mock.getScanStatus()).thenReturn(XrayWrapperArtifactoryClientSupport.ScanStatus.UNKNOWN);
            when(mock.requestScanReports()).thenReturn(false);
        })) {
            clientSupportToTest = new XrayWrapperArtifactoryClientSupport(configuration, artifact);
            assertThrows(XrayWrapperException.class, () -> clientSupportToTest.waitForScansToFinishAndDownloadReport());
        }
    }
}