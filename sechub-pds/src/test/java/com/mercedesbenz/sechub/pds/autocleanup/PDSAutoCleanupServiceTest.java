// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.autocleanup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.pds.config.PDSConfigService;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;
import com.mercedesbenz.sechub.pds.time.PDSTimeCalculationService;

class PDSAutoCleanupServiceTest {

    private PDSAutoCleanupService serviceToTest;
    private PDSConfigService configService;
    private PDSJobRepository jobRepository;
    private PDSTimeCalculationService pdsTimeCalculationService;
    private PDSAutoCleanupResultInspector inspector;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new PDSAutoCleanupService();

        configService = mock(PDSConfigService.class);
        jobRepository = mock(PDSJobRepository.class);
        pdsTimeCalculationService = mock(PDSTimeCalculationService.class);
        inspector = mock(PDSAutoCleanupResultInspector.class);

        serviceToTest.configService = configService;
        serviceToTest.jobRepository = jobRepository;
        serviceToTest.PDSTimeCalculationService = pdsTimeCalculationService;
        serviceToTest.inspector = inspector;
    }

    @Test
    void cleanup_executes_NOT_delete_job_when_config_service_returns_minus_1_day() {
        /* prepare */
        long days = -1;
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(pdsTimeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(pdsTimeCalculationService, never()).calculateNowMinusDays(any());
        verify(jobRepository, never()).deleteJobOlderThan(any());
        // check inspection as expected: never because not executed
        verify(inspector, never()).inspect(any());
    }

    @Test
    void cleanup_executes_NOT_delete_job_when_config_servic_returns_0_days() {
        /* prepare */
        long days = 0;
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(pdsTimeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(pdsTimeCalculationService, never()).calculateNowMinusDays(any());
        verify(jobRepository, never()).deleteJobOlderThan(any());
        // check inspection as expected: never because not executed
        verify(inspector, never()).inspect(any());
    }

    @ParameterizedTest
    @CsvSource({ "1", "2,", "30", "200" })
    void cleanup_executes_delete_job_when_config_service_returns_days_bigger_than_zero(long days) {
        /* prepare */
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(pdsTimeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);
        when(jobRepository.deleteJobOlderThan(cleanTime)).thenReturn(1234);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(pdsTimeCalculationService).calculateNowMinusDays(eq(days));
        verify(jobRepository).deleteJobOlderThan(cleanTime);

        // check inspection as expected
        ArgumentCaptor<PDSAutoCleanupResult> captor = ArgumentCaptor.forClass(PDSAutoCleanupResult.class);
        verify(inspector).inspect(captor.capture());

        PDSAutoCleanupResult result = captor.getValue();
        assertEquals(cleanTime, result.getUsedCleanupTimeStamp());
        assertEquals(days, result.getCleanupTimeInDays());
        assertEquals(1234, result.getDeletedEntries());
    }

}
