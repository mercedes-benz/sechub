// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.wrapper.xray.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.io.File;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

import com.mercedesbenz.sechub.wrapper.xray.XrayWrapperException;
import com.mercedesbenz.sechub.wrapper.xray.api.XrayAPIArtifactoryClient;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperArtifact;
import com.mercedesbenz.sechub.wrapper.xray.config.XrayWrapperConfiguration;
import com.mercedesbenz.sechub.wrapper.xray.report.XrayWrapperReportSupport;

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
    void waitForScansToFinishAndDownloadReport_valid_execution() throws XrayWrapperException {
        /* prepare */
        final XrayAPIArtifactoryClient[] lamdaReference = new XrayAPIArtifactoryClient[1];
        XrayWrapperReportSupport.XrayReportFiles files = new XrayWrapperReportSupport.XrayReportFiles(new File("any"), new File("any"), new File("any"));
        try (MockedConstruction<XrayAPIArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayAPIArtifactoryClient.class, (mock, context) -> {
            lamdaReference[0] = mock;
            when(mock.requestXrayVersion()).thenReturn("mocked-version");
            when(mock.getScanStatus()).thenReturn(XrayWrapperArtifactoryClientSupport.ScanStatus.SCANNED);
            when(mock.requestScanReports()).thenReturn(true);
        })) {
            XrayWrapperReportSupport reportSupport = mock(XrayWrapperReportSupport.class);
            when(reportSupport.collectXrayReportsInArchive(any(), any())).thenReturn(files);
            doNothing().when(reportSupport).writeReport(any(), any());
            clientSupportToTest = new XrayWrapperArtifactoryClientSupport(configuration, artifact);
            clientSupportToTest.reportSupport = reportSupport;

            /* execute */
            clientSupportToTest.waitForScansToFinishAndDownloadReport();

            /* test */
            verify(lamdaReference[0], times(1)).assertArtifactoryUploadSuccess();
            verify(lamdaReference[0], times(1)).deleteArtifact();
            verify(lamdaReference[0], times(1)).deleteUploads();
            verify(reportSupport, times(1)).writeReport(any(), any());
        }
    }

    @Test
    void waitForScansToFinishAndDownloadReport_valid_execution_without_report_transformation() throws XrayWrapperException {
        /* prepare */
        final XrayAPIArtifactoryClient[] lamdaReference = new XrayAPIArtifactoryClient[1];
        try (MockedConstruction<XrayAPIArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayAPIArtifactoryClient.class, (mock, context) -> {
            lamdaReference[0] = mock;
            when(mock.requestXrayVersion()).thenReturn("mocked-version");
            when(mock.getScanStatus()).thenReturn(XrayWrapperArtifactoryClientSupport.ScanStatus.SCANNED);
            when(mock.requestScanReports()).thenReturn(false);
        })) {
            XrayWrapperReportSupport reportSupport = mock(XrayWrapperReportSupport.class);
            clientSupportToTest = new XrayWrapperArtifactoryClientSupport(configuration, artifact);
            clientSupportToTest.reportSupport = reportSupport;

            /* execute */
            clientSupportToTest.waitForScansToFinishAndDownloadReport();

            /* test */
            verify(lamdaReference[0], times(1)).assertArtifactoryUploadSuccess();
            verify(lamdaReference[0], times(1)).deleteArtifact();
            verify(lamdaReference[0], times(1)).deleteUploads();
            verify(reportSupport, never()).writeReport(any(), any());
        }
    }

    @Test
    void waitForScansToFinishAndDownloadReport_artifact_unknown_scan_status_throws_XrayWrapperException() {
        /* prepare */
        try (MockedConstruction<XrayAPIArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayAPIArtifactoryClient.class, (mock, context) -> {
            when(mock.requestXrayVersion()).thenReturn("mocked-version");
            when(mock.getScanStatus()).thenReturn(XrayWrapperArtifactoryClientSupport.ScanStatus.UNKNOWN);
            when(mock.requestScanReports()).thenReturn(false);
        })) {
            clientSupportToTest = new XrayWrapperArtifactoryClientSupport(configuration, artifact);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientSupportToTest.waitForScansToFinishAndDownloadReport());

            /* test */
            assertEquals("Received unexpected scan status", exception.getMessage());
        }
    }

    @Test
    void waitForScansToFinishAndDownloadReport_artifact_not_uploaded_throws_XrayWrapperException() {
        /* prepare */
        try (MockedConstruction<XrayAPIArtifactoryClient> mockedClient = Mockito.mockConstruction(XrayAPIArtifactoryClient.class, (mock, context) -> {
            when(mock.requestXrayVersion()).thenReturn("mocked-version");
            doThrow(new XrayWrapperException("Artifact not uploaded", XrayWrapperExitCode.ARTIFACTORY_ERROR_RESPONSE)).when(mock)
                    .assertArtifactoryUploadSuccess();
            when(mock.getScanStatus()).thenReturn(XrayWrapperArtifactoryClientSupport.ScanStatus.UNKNOWN);
            when(mock.requestScanReports()).thenReturn(false);
        })) {
            clientSupportToTest = new XrayWrapperArtifactoryClientSupport(configuration, artifact);

            /* execute */
            XrayWrapperException exception = assertThrows(XrayWrapperException.class, () -> clientSupportToTest.waitForScansToFinishAndDownloadReport());

            /* test */
            assertEquals("Artifact not uploaded", exception.getMessage());
        }
    }
}