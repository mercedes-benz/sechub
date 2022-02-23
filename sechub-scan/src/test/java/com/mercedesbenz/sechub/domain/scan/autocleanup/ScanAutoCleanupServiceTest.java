package com.mercedesbenz.sechub.domain.scan.autocleanup;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.scan.config.ScanConfigService;
import com.mercedesbenz.sechub.domain.scan.log.ProjectScanLogRepository;
import com.mercedesbenz.sechub.domain.scan.product.ProductResultRepository;
import com.mercedesbenz.sechub.domain.scan.report.ScanReportRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;

class ScanAutoCleanupServiceTest {

    private ScanAutoCleanupService serviceToTest;
    private ScanConfigService configService;
    private TimeCalculationService timeCalculationService;
    private ProductResultRepository productResultRepository;
    private ProjectScanLogRepository projectScanLogRepository;
    private ScanReportRepository scanReportRepository;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new ScanAutoCleanupService();

        configService = mock(ScanConfigService.class);
        timeCalculationService = mock(TimeCalculationService.class);
        productResultRepository = mock(ProductResultRepository.class);
        projectScanLogRepository = mock(ProjectScanLogRepository.class);
        scanReportRepository = mock(ScanReportRepository.class);

        serviceToTest.configService = configService;
        serviceToTest.productResultRepository = productResultRepository;
        serviceToTest.projectScanLogRepository = projectScanLogRepository;
        serviceToTest.scanReportRepository = scanReportRepository;

        serviceToTest.timeCalculationService = timeCalculationService;
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
    }

    @Test
    void cleanup_executes_delete_job_information_for_30_days() {
        /* prepare */
        long days = 30;
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(timeCalculationService).calculateNowMinusDays(eq(days));
        verify(productResultRepository, times(1)).deleteResultsOlderThan(cleanTime);
        verify(projectScanLogRepository, times(1)).deleteLogsOlderThan(cleanTime);
        // as long as issue https://github.com/mercedes-benz/sechub/issues/1010 is not
        // implemented we keep the old data for statistics, so never called:
        verify(scanReportRepository, never()).deleteReportsOlderThan(cleanTime);
    }

}
