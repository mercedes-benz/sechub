package com.mercedesbenz.sechub.domain.administration.autocleanup;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.domain.administration.job.JobInformationRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;

class AdministrationAutoCleanupServiceTest {

    private AdministrationAutoCleanupService serviceToTest;
    private AdministrationConfigService configService;
    private JobInformationRepository jobInformationRepository;
    private TimeCalculationService timeCalculationService;
    private AutoCleanupResultInspector inspector;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new AdministrationAutoCleanupService();

        configService = mock(AdministrationConfigService.class);
        jobInformationRepository = mock(JobInformationRepository.class);
        timeCalculationService = mock(TimeCalculationService.class);
        inspector = mock(AutoCleanupResultInspector.class);

        serviceToTest.configService = configService;
        serviceToTest.jobInformationRepository = jobInformationRepository;
        serviceToTest.timeCalculationService = timeCalculationService;
        serviceToTest.inspector = inspector;
    }

    @Test
    void cleanup_executes_NOT_delete_job_information_for_minus_1_day() {
        /* prepare */
        long days = -1;
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(timeCalculationService, never()).calculateNowMinusDays(any());
        verify(jobInformationRepository, never()).deleteJobInformationOlderThan(any());
        // check inspection as expected: never because not executed
        verify(inspector, never()).inspect(any());
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
        when(jobInformationRepository.deleteJobInformationOlderThan(cleanTime)).thenReturn(1234);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(configService).getAutoCleanupInDays();
        verify(timeCalculationService).calculateNowMinusDays(eq(days));
        verify(jobInformationRepository).deleteJobInformationOlderThan(cleanTime);

        // check inspection as expected
        ArgumentCaptor<AutoCleanupResult> captor = ArgumentCaptor.forClass(AutoCleanupResult.class);
        verify(inspector).inspect(captor.capture());

        AutoCleanupResult result = captor.getValue();
        assertEquals(cleanTime, result.getUsedCleanupTimeStamp());
        assertEquals(days, result.getCleanupTimeInDays());
        assertEquals(1234, result.getDeletedEntries());
    }

}
