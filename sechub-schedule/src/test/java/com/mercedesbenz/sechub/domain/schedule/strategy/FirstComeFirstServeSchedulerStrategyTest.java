// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

class FirstComeFirstServeSchedulerStrategyTest {

    private FirstComeFirstServeSchedulerStrategy strategyToTest;
    private SecHubJobRepository jobRepository;
    private UUID jobUUID;

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();
        jobRepository = mock(SecHubJobRepository.class);

        strategyToTest = new FirstComeFirstServeSchedulerStrategy();
        strategyToTest.jobRepository = jobRepository;
    }

    @Test
    void nextJobId_calls_expected_query_method() {
        /* prepare */
        when(jobRepository.nextJobIdToExecuteFirstInFirstOut()).thenReturn(Optional.of(jobUUID));

        /* execute */
        UUID result = strategyToTest.nextJobId();

        /* test */
        assertEquals(jobUUID, result);
        verify(jobRepository).nextJobIdToExecuteFirstInFirstOut();
    }

}
