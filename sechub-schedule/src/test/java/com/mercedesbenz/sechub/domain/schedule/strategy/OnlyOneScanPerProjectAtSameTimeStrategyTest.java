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

import com.mercedesbenz.sechub.domain.schedule.encryption.ScheduleEncryptionService;
import com.mercedesbenz.sechub.domain.schedule.job.SecHubJobRepository;

class OnlyOneScanPerProjectAtSameTimeStrategyTest {

    private OnlyOneScanPerProjectAtSameTimeStrategy strategyToTest;
    private SecHubJobRepository jobRepository;
    private UUID jobUUID;
    private ScheduleEncryptionService encryptionService;

    @BeforeEach
    void beforeEach() {
        jobUUID = UUID.randomUUID();
        jobRepository = mock(SecHubJobRepository.class);

        encryptionService = mock(ScheduleEncryptionService.class);

        strategyToTest = new OnlyOneScanPerProjectAtSameTimeStrategy();
        strategyToTest.jobRepository = jobRepository;
        strategyToTest.encryptionService = encryptionService;
    }

    @Test
    void nextJobId_calls_expected_query_method() {
        /* prepare */
        Set<Long> set = Collections.emptySet();
        when(jobRepository.nextJobIdToExecuteForProjectNotYetExecuted(set)).thenReturn(Optional.of(jobUUID));

        /* execute */
        UUID result = strategyToTest.nextJobId();

        /* test */
        assertEquals(jobUUID, result);
        verify(jobRepository).nextJobIdToExecuteForProjectNotYetExecuted(set);
    }

}
