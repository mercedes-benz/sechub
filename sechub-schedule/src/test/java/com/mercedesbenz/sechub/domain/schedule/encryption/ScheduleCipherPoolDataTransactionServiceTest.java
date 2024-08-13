// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ScheduleCipherPoolDataTransactionServiceTest {

    private ScheduleCipherPoolDataTransactionService serviceToTest;
    private ScheduleCipherPoolDataRepository repository;

    @BeforeEach
    void beforeEach() {

        repository = mock(ScheduleCipherPoolDataRepository.class);

        serviceToTest = new ScheduleCipherPoolDataTransactionService();

        serviceToTest.repository = repository;

    }

    @Test
    void storeInOwnTransaction_calls_repository_save() throws Exception {

        /* prepare */
        ScheduleCipherPoolData data = mock(ScheduleCipherPoolData.class);

        /* execute */
        serviceToTest.storeInOwnTransaction(data);

        /* test */
        verify(repository).save(data);

    }

    @Test
    void storeInOwnTransaction_returns_repository_object() throws Exception {
        /* prepare */
        ScheduleCipherPoolData data = mock(ScheduleCipherPoolData.class);
        when(repository.save(data)).thenReturn(data);

        /* execute */
        ScheduleCipherPoolData result = serviceToTest.storeInOwnTransaction(data);

        /* test */
        assertThat(result).isEqualTo(data);

    }

}
