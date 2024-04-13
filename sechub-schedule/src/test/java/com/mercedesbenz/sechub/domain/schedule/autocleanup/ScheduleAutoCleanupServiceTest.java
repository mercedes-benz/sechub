// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.autocleanup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobDataRepository;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;

class ScheduleAutoCleanupServiceTest {

    private ScheduleAutoCleanupService serviceToTest;
    private SchedulerConfigService configService;
    private SecHubJobRepository jobRepository;
    private SecHubJobDataRepository jobDataRepository;
    private TimeCalculationService timeCalculationService;
    private AutoCleanupResultInspector inspector;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new ScheduleAutoCleanupService();

        configService = mock(SchedulerConfigService.class);
        jobRepository = mock(SecHubJobRepository.class);
        jobDataRepository = mock(SecHubJobDataRepository.class);
        timeCalculationService = mock(TimeCalculationService.class);
        inspector = mock(AutoCleanupResultInspector.class);

        serviceToTest.configService = configService;
        serviceToTest.jobRepository = jobRepository;
        serviceToTest.jobDataRepository = jobDataRepository;
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
        verify(jobRepository, never()).deleteJobsOlderThan(any());
        verify(jobDataRepository, never()).deleteJobDataOlderThan(any());
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

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(timeCalculationService).calculateNowMinusDays(eq(days));
        verify(jobRepository).deleteJobsOlderThan(cleanTime);
        verify(jobDataRepository).deleteJobDataOlderThan(cleanTime);

        // check inspection as expected
        ArgumentCaptor<AutoCleanupResult> captor = ArgumentCaptor.forClass(AutoCleanupResult.class);
        verify(inspector).inspect(captor.capture());

        AutoCleanupResult result = captor.getValue();
        assertEquals(cleanTime, result.getUsedCleanupTimeStamp());
        assertEquals(days, result.getCleanupTimeInDays());
    }

}
