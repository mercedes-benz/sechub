package com.mercedesbenz.sechub.domain.schedule.autocleanup;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;

class ScheduleAutoCleanupServiceTest {

    private ScheduleAutoCleanupService serviceToTest;
    private SchedulerConfigService configService;
    private SecHubJobRepository jobRepository;
    private TimeCalculationService timeCalculationService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new ScheduleAutoCleanupService();

        configService = mock(SchedulerConfigService.class);
        jobRepository = mock(SecHubJobRepository.class);
        timeCalculationService = mock(TimeCalculationService.class);

        serviceToTest.configService = configService;
        serviceToTest.jobRepository = jobRepository;
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
        verify(jobRepository, never()).deleteJobsOlderThan(any());
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
    }

}
