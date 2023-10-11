// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.scan.autocleanup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.scan.config.ScanConfigService;
import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogRepository;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;

class ScanAutoCleanupServiceTest {

    private ScanAutoCleanupService serviceToTest;
    private ScanConfigService configService;
    private TimeCalculationService timeCalculationService;
    private ProductResultRepository productResultRepository;
    private ProjectScanLogRepository projectScanLogRepository;
    private ScanReportRepository scanReportRepository;
    private AutoCleanupResultInspector inspector;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new ScanAutoCleanupService();

        configService = mock(ScanConfigService.class);
        timeCalculationService = mock(TimeCalculationService.class);
        productResultRepository = mock(ProductResultRepository.class);
        projectScanLogRepository = mock(ProjectScanLogRepository.class);
        scanReportRepository = mock(ScanReportRepository.class);
        inspector = mock(AutoCleanupResultInspector.class);

        serviceToTest.configService = configService;
        serviceToTest.productResultRepository = productResultRepository;
        serviceToTest.projectScanLogRepository = projectScanLogRepository;
        serviceToTest.scanReportRepository = scanReportRepository;
        serviceToTest.timeCalculationService = timeCalculationService;
        serviceToTest.inspector = inspector;
    }

    @Test
    void cleanup_executes_NOT_delete_job_information_for_0_days() {
        /* prepare */
        long days = 0;
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(timeCalculationService, never()).calculateNowMinusDays(any());
        verify(productResultRepository, never()).deleteResultsOlderThan(cleanTime);
        verify(projectScanLogRepository, never()).deleteLogsOlderThan(cleanTime);
        verify(scanReportRepository, never()).deleteReportsOlderThan(cleanTime);
        // check inspection as expected: never because not executed
        verify(inspector, never()).inspect(any());
    }

    @Test
    void cleanup_executes_delete_job_information_for_30_days() {
        /* prepare */
        long days = 30;
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        when(projectScanLogRepository.deleteLogsOlderThan(cleanTime)).thenReturn(10);
        when(productResultRepository.deleteResultsOlderThan(cleanTime)).thenReturn(20);
        when(scanReportRepository.deleteReportsOlderThan(cleanTime)).thenReturn(30);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(timeCalculationService).calculateNowMinusDays(eq(days));
        verify(productResultRepository, times(1)).deleteResultsOlderThan(cleanTime);
        verify(projectScanLogRepository, times(1)).deleteLogsOlderThan(cleanTime);
        verify(scanReportRepository, times(1)).deleteReportsOlderThan(cleanTime);

        // check inspection as expected
        ArgumentCaptor<AutoCleanupResult> captor = ArgumentCaptor.forClass(AutoCleanupResult.class);
        verify(inspector, times(3)).inspect(captor.capture());

        List<AutoCleanupResult> values = captor.getAllValues();
        for (AutoCleanupResult result : values) {
            assertEquals(cleanTime, result.getUsedCleanupTimeStamp());
            assertEquals(days, result.getCleanupTimeInDays());

            String variant = result.getKey().getVariant();

            switch (variant) {
            case "scan-logs":
                assertEquals(10, result.getDeletedEntries());
                break;
            case "product-results":
                assertEquals(20, result.getDeletedEntries());
                break;
            case "scan-reports":
                assertEquals(30, result.getDeletedEntries());
                break;
            default:
                fail("unexpected variant:" + variant);
            }
        }
    }

}
