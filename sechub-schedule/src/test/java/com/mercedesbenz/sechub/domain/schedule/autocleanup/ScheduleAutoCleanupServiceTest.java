// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.autocleanup;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.domain.schedule.config.SchedulerConfigService;
import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleCipherPoolCleanupService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobDataRepository;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;
import com.mercedesbenz.sechub.sharedkernel.TimeCalculationService;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResult;
import com.mercedesbenz.sechub.sharedkernel.autocleanup.AutoCleanupResultInspector;
import com.mercedesbenz.sechub.sharedkernel.storage.SecHubStorageService;
import com.mercedesbenz.sechub.storage.core.JobStorage;
import com.mercedesbenz.sechub.test.TestCanaryException;

class ScheduleAutoCleanupServiceTest {

    private ScheduleAutoCleanupService serviceToTest;
    private SchedulerConfigService configService;
    private SecHubJobRepository jobRepository;
    private SecHubJobDataRepository jobDataRepository;
    private TimeCalculationService timeCalculationService;
    private AutoCleanupResultInspector inspector;
    private ScheduleCipherPoolCleanupService encryptionPoolCleanupService;
    private SecHubStorageService storageService;
    private JobStorage jobStorage;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new ScheduleAutoCleanupService();

        configService = mock(SchedulerConfigService.class);
        jobRepository = mock(SecHubJobRepository.class);
        jobDataRepository = mock(SecHubJobDataRepository.class);
        timeCalculationService = mock(TimeCalculationService.class);
        inspector = mock(AutoCleanupResultInspector.class);
        encryptionPoolCleanupService = mock(ScheduleCipherPoolCleanupService.class);
        storageService = mock(SecHubStorageService.class);

        jobStorage = mock(JobStorage.class);

        serviceToTest.configService = configService;
        serviceToTest.jobRepository = jobRepository;
        serviceToTest.jobDataRepository = jobDataRepository;
        serviceToTest.timeCalculationService = timeCalculationService;
        serviceToTest.inspector = inspector;
        serviceToTest.encryptionPoolCleanupService = encryptionPoolCleanupService;
        serviceToTest.storageService = storageService;
    }

    @Test
    void auto_cleanup_triggers_encryption_pool_cleanup() throws Exception {

        /* prepare */
        when(configService.getAutoCleanupInDays()).thenReturn(1L);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(1L);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        /* execute */
        serviceToTest.cleanup();

        /* test */
        verify(encryptionPoolCleanupService).cleanupCipherPoolDataIfNecessaryAndPossible();

    }

    @Test
    void when_jobDataRepository_deleteJobDataOlderThan_throws_exception_encryption_pool_cleanup_is_not_done() throws Exception {

        /* prepare */
        when(configService.getAutoCleanupInDays()).thenReturn(1L);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(1L);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);
        when(jobDataRepository.deleteJobDataOlderThan(cleanTime)).thenThrow(TestCanaryException.class);

        /* execute */
        assertThatThrownBy(()->serviceToTest.cleanup()).isInstanceOf(TestCanaryException.class);

        /* test */
        verify(encryptionPoolCleanupService,never()).cleanupCipherPoolDataIfNecessaryAndPossible();

    }

    @Test
    void when_jobRepository_deleteJobsOlderThan_throws_exception_encryption_pool_cleanup_is_not_done() throws Exception {

        /* prepare */
        when(configService.getAutoCleanupInDays()).thenReturn(1L);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(1L);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);
        when(jobRepository.deleteJobsOlderThan(cleanTime)).thenThrow(TestCanaryException.class);

        /* execute */
        assertThatThrownBy(()->serviceToTest.cleanup()).isInstanceOf(TestCanaryException.class);

        /* test */
        verify(encryptionPoolCleanupService,never()).cleanupCipherPoolDataIfNecessaryAndPossible();

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
        // check not encryption pool cleanup is done
        verify(encryptionPoolCleanupService, never()).cleanupCipherPoolDataIfNecessaryAndPossible();

        verify(jobRepository, never()).findJobUUIDsAndProjectIdsForJobsOlderThan(any());
    }

    @Test
    void cleanup_executes_delete_job_information_for_30_days() throws Exception {
        /* prepare */
        long days = 30;
        when(configService.getAutoCleanupInDays()).thenReturn(days);
        LocalDateTime cleanTime = LocalDateTime.now().minusDays(days);
        when(timeCalculationService.calculateNowMinusDays(any())).thenReturn(cleanTime);

        UUID jobUUID = UUID.randomUUID();
        List<Object[]> jobUUIDsAndProjectIdsOfOlderJobs = new ArrayList<>();
        jobUUIDsAndProjectIdsOfOlderJobs.add(new Object[] { jobUUID, "project-id" });

        when(jobRepository.findJobUUIDsAndProjectIdsForJobsOlderThan(cleanTime)).thenReturn(jobUUIDsAndProjectIdsOfOlderJobs);
        when(storageService.createJobStorageForProject("project-id", jobUUID)).thenReturn(jobStorage);

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

        verify(jobRepository).findJobUUIDsAndProjectIdsForJobsOlderThan(cleanTime);
        verify(jobStorage).deleteAll();
    }

}
