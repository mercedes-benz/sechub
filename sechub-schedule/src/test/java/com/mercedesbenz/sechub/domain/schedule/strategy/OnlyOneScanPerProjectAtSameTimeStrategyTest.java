// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.strategy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

class OnlyOneScanPerProjectAtSameTimeStrategyTest {

    private OnlyOneScanPerProjectAtSameTimeStrategy strategyToTest;
    private SecHubJobRepository jobRepository;
    private UUID jobUUID;

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();
        jobRepository = mock(SecHubJobRepository.class);

        strategyToTest = new OnlyOneScanPerProjectAtSameTimeStrategy();
        strategyToTest.jobRepository = jobRepository;
    }

    @Test
    void nextJobId_calls_expected_query_method() {
        /* prepare */
        Set<Long> currentEncryptionPoolIds = Collections.emptySet();
        when(jobRepository.nextJobIdToExecuteForProjectNotYetExecuted(currentEncryptionPoolIds)).thenReturn(Optional.of(jobUUID));

        /* execute */
        Optional<UUID> result = strategyToTest.nextJobId(currentEncryptionPoolIds);

        /* test */
        verify(jobRepository).nextJobIdToExecuteForProjectNotYetExecuted(currentEncryptionPoolIds);
        assertTrue(result.isPresent());
        assertEquals(jobUUID, result.get());
    }

}
