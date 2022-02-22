package com.mercedesbenz.sechub.domain.administration.autocleanup;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.administration.config.AdministrationConfigService;
import com.mercedesbenz.sechub.domain.administration.job.JobInformationRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;

class AutoCleanupServiceTest {

    private AutoCleanupService serviceToTest;
    private AdministrationConfigService configService;
    private JobInformationRepository jobInformationRepository;
    private TimeCalculationService timeCalculationService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new AutoCleanupService();

        configService = mock(AdministrationConfigService.class);
        jobInformationRepository = mock(JobInformationRepository.class);
        timeCalculationService = mock(TimeCalculationService.class);

        serviceToTest.configService = configService;
        serviceToTest.jobInformationRepository = jobInformationRepository;
        serviceToTest.timeCalculationService = timeCalculationService;
    }

    @Test
    void cleanup_executes_delete_job_information_old_than_with_calculated_clean_time() {
        /* prepare */
        LocalDateTime cleanTime = LocalDateTime.now();
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(jobInformationRepository).deleteJobInformationOlderThan(cleanTime);
    }

}
