package com.mercedesbenz.sechub.domain.administration.autocleanup;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.domain.administration.job.JobInformationRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;

class AdministrationAutoCleanupServiceTest {

    private AdministrationAutoCleanupService serviceToTest;
    private AdministrationConfigService configService;
    private JobInformationRepository jobInformationRepository;
    private TimeCalculationService timeCalculationService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new AdministrationAutoCleanupService();

        configService = mock(AdministrationConfigService.class);
        jobInformationRepository = mock(JobInformationRepository.class);
        timeCalculationService = mock(TimeCalculationService.class);

        serviceToTest.configService = configService;
        serviceToTest.jobInformationRepository = jobInformationRepository;
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
        verify(jobInformationRepository, never()).deleteJobInformationOlderThan(any());
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
        verify(jobInformationRepository).deleteJobInformationOlderThan(cleanTime);
    }

}
