// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

class SchedulerNextJobResolverTest {

    private SchedulerNextJobResolver resolverToTest;
    private SecHubJobRepository jobRepository;
    private UUID jobUUID;
    private ScheduleEncryptionService encryptionService;
    private SchedulerStrategyProvider schedulerStrategyProvider;
    private SchedulerStrategy strategy;

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();
        jobRepository = mock(SecHubJobRepository.class);
        schedulerStrategyProvider = mock(SchedulerStrategyProvider.class);
        strategy = mock(SchedulerStrategy.class);

        encryptionService = mock(ScheduleEncryptionService.class);

        resolverToTest = new SchedulerNextJobResolver();
        resolverToTest.jobRepository = jobRepository;
        resolverToTest.encryptionService = encryptionService;
        resolverToTest.schedulerStrategyProvider = schedulerStrategyProvider;

        when(schedulerStrategyProvider.getStrategy()).thenReturn(strategy);
    }

    @Test
    void resolveNextJob_uses_encryption_service_when_no_encryption_pool_ids_available_no_interaction_and_always_null() {
        /* prepare */
        Set<Long> currentEncryptionPoolIds = Set.of(); // empty...
        when(encryptionService.getCurrentEncryptionPoolIds()).thenReturn(currentEncryptionPoolIds);
        when(jobRepository.nextJobIdToExecuteSuspended(currentEncryptionPoolIds, resolverToTest.minimumSuspendDurationInMilliseconds))
                .thenReturn(Optional.of(jobUUID));

        /* execute */
        UUID result = resolverToTest.resolveNextJobUUID();

        /* test */
        verify(jobRepository, never()).nextJobIdToExecuteSuspended(anySet(), anyLong());
        verify(strategy, never()).nextJobId(anySet());
        assertEquals(null, result);
    }

    @Test
    void resolveNextJob_uses_encryption_service_and_fetches_suspended_job() {
        /* prepare */
        Set<Long> currentEncryptionPoolIds = Set.of(1L);
        when(encryptionService.getCurrentEncryptionPoolIds()).thenReturn(currentEncryptionPoolIds);
        when(jobRepository.nextJobIdToExecuteSuspended(currentEncryptionPoolIds, resolverToTest.minimumSuspendDurationInMilliseconds))
                .thenReturn(Optional.of(jobUUID));

        /* execute */
        UUID result = resolverToTest.resolveNextJobUUID();

        /* test */
        verify(jobRepository).nextJobIdToExecuteSuspended(currentEncryptionPoolIds, resolverToTest.minimumSuspendDurationInMilliseconds);
        verify(strategy, never()).nextJobId(currentEncryptionPoolIds);
        assertEquals(jobUUID, result);
    }

    @Test
    void resolveNextJob_uses_encryption_service_no_suspended_jobs_found_then_job_from_strategy_is_used() {
        /* prepare */
        Set<Long> currentEncryptionPoolIds = Set.of(1L);
        when(encryptionService.getCurrentEncryptionPoolIds()).thenReturn(currentEncryptionPoolIds);
        when(jobRepository.nextJobIdToExecuteSuspended(currentEncryptionPoolIds, resolverToTest.minimumSuspendDurationInMilliseconds))
                .thenReturn(Optional.empty());
        when(strategy.nextJobId(currentEncryptionPoolIds)).thenReturn(Optional.of(jobUUID));

        /* execute */
        UUID result = resolverToTest.resolveNextJobUUID();

        /* test */
        verify(jobRepository).nextJobIdToExecuteSuspended(currentEncryptionPoolIds, resolverToTest.minimumSuspendDurationInMilliseconds);
        verify(strategy).nextJobId(currentEncryptionPoolIds);
        assertEquals(jobUUID, result);
    }

    @Test
    void resolveNextJob_uses_encryption_service_and_fetches_no_suspended_jobs_when_found_job_from_strategy_is_used_but_also_not_found() {
        /* prepare */
        Set<Long> currentEncryptionPoolIds = Set.of(1L);
        when(encryptionService.getCurrentEncryptionPoolIds()).thenReturn(currentEncryptionPoolIds);
        when(jobRepository.nextJobIdToExecuteSuspended(currentEncryptionPoolIds, resolverToTest.minimumSuspendDurationInMilliseconds))
                .thenReturn(Optional.empty());
        when(strategy.nextJobId(currentEncryptionPoolIds)).thenReturn(Optional.empty());

        /* execute */
        UUID result = resolverToTest.resolveNextJobUUID();

        /* test */
        verify(jobRepository).nextJobIdToExecuteSuspended(currentEncryptionPoolIds, resolverToTest.minimumSuspendDurationInMilliseconds);
        verify(strategy).nextJobId(currentEncryptionPoolIds);
        assertEquals(null, result);
    }

}
