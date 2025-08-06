// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.report;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mercedesbenz.sechub.domain.scan.ScanAssertService;
import com.mercedesbenz.sechub.domain.scan.SecHubReportProductTransformerService;
import com.mercedesbenz.sechub.sharedkernel.error.NotFoundException;
import com.mercedesbenz.sechub.sharedkernel.logging.AuditLogService;
import com.mercedesbenz.sechub.sharedkernel.validation.UserInputAssertion;

@ExtendWith(MockitoExtension.class)
public class DownloadScanReportServiceTest {

    @Mock
    private ScanAssertService scanAssertService;

    @Mock
    private SecHubReportProductTransformerService secHubResultService;

    @Mock
    private ScanReportRepository reportRepository;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private UserInputAssertion assertion;

    @Mock
    private ScanReportSensitiveDataObfuscator scanReportSensitiveDataObfuscator;

    @InjectMocks
    private DownloadScanReportService downloadScanReportService;

    @BeforeAll
    static void beforeAll() {
        // Global setup if needed
    }

    @BeforeEach
    void beforeEach() {
        // Setup before each test
    }

    @Test
    void getObfuscatedScanSecHubReport_validInputs_returnsObfuscatedReport() {
        /* prepare */
        String projectId = "validProjectId";
        UUID jobUUID = UUID.randomUUID();
        ScanReport mockReport = mock(ScanReport.class);
        when(mockReport.getProjectId()).thenReturn(projectId);
        when(reportRepository.findBySecHubJobUUID(jobUUID)).thenReturn(mockReport);

        /* execute + test */
        ScanSecHubReport result = downloadScanReportService.getObfuscatedScanSecHubReport(projectId, jobUUID);

        /* test */
        assertThat(result).isNotNull();
        verify(scanReportSensitiveDataObfuscator).obfuscate(result);
    }

    @Test
    void getObfuscatedScanSecHubReport_reportNotFound_throwsNotFoundException() {
        /* prepare */
        String projectId = "validProjectId";
        UUID jobUUID = UUID.randomUUID();
        when(reportRepository.findBySecHubJobUUID(jobUUID)).thenReturn(null);

        /* execute + test */
        assertThatThrownBy(() -> downloadScanReportService.getObfuscatedScanSecHubReport(projectId, jobUUID)).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Report not found");
    }

    @Test
    void getObfuscatedScanSecHubReport_jobNotForGivenProject_throwsNotFoundException() {
        /* prepare */
        String projectId = "validProjectId";
        UUID jobUUID = UUID.randomUUID();
        ScanReport mockReport = mock(ScanReport.class);
        when(mockReport.getProjectId()).thenReturn("differentProjectId");
        when(reportRepository.findBySecHubJobUUID(jobUUID)).thenReturn(mockReport);

        /* execute + test */
        assertThatThrownBy(() -> downloadScanReportService.getObfuscatedScanSecHubReport(projectId, jobUUID)).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Job is not for the given project");
    }

    @Test
    void getObfuscatedScanSecHubReport_userHasNoAccessToReport_throwsNotFoundException() {
        /* prepare */
        String projectId = "validProjectId";
        UUID jobUUID = UUID.randomUUID();
        ScanReport mockReport = mock(ScanReport.class);
        when(mockReport.getProjectId()).thenReturn(projectId);
        when(reportRepository.findBySecHubJobUUID(jobUUID)).thenReturn(mockReport);
        doThrow(new NotFoundException("No access")).when(scanAssertService).assertUserHasAccessToReport(mockReport);

        /* execute + test */
        assertThatThrownBy(() -> downloadScanReportService.getObfuscatedScanSecHubReport(projectId, jobUUID)).isInstanceOf(NotFoundException.class)
                .hasMessageContaining("No access");
    }
}