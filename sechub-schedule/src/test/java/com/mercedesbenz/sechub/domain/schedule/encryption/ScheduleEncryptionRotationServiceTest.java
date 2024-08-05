// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherAlgorithm;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubCipherPasswordSourceType;
import com.mercedesbenz.sechub.sharedkernel.encryption.SecHubEncryptionData;

class ScheduleEncryptionRotationServiceTest {

    private ScheduleEncryptionRotationService serviceToTest;
    private ScheduleEncryptionService encryptionService;
    private ScheduleCipherPoolDataTransactionService transactionService;

    @BeforeEach
    void beforeEach() {

        encryptionService = mock(ScheduleEncryptionService.class);

        serviceToTest = new ScheduleEncryptionRotationService();

        transactionService = mock(ScheduleCipherPoolDataTransactionService.class);
        serviceToTest.transactionService = transactionService;
        serviceToTest.encryptionService = encryptionService;

    }

    @Test
    void createInitialCipherPoolData_stores_initial_cipher_pooldata_from_encryption_service_together_with_user_info() throws Exception {

        /* prepare */
        SecHubEncryptionData data = new SecHubEncryptionData();
        data.setAlgorithm(SecHubCipherAlgorithm.AES_GCM_SIV_128);
        data.setPasswordSourceData("data1");
        data.setPasswordSourceType(SecHubCipherPasswordSourceType.ENVIRONMENT_VARIABLE);

        ScheduleCipherPoolData createdPoolData = new ScheduleCipherPoolData();
        when(encryptionService.createInitialCipherPoolData(eq(data), any())).thenReturn(createdPoolData);
        when(transactionService.storeInOwnTransaction(createdPoolData)).thenReturn(createdPoolData);

        /* execute */
        serviceToTest.startEncryptionRotation(data, "user1");

        /* test */
        ArgumentCaptor<ScheduleCipherPoolData> entityCaptor = ArgumentCaptor.forClass(ScheduleCipherPoolData.class);
        verify(transactionService).storeInOwnTransaction(entityCaptor.capture());

        ScheduleCipherPoolData persistedPoolData = entityCaptor.getValue();
        assertThat(persistedPoolData.getCreatedFrom()).isEqualTo("user1"); // the stored entity contains the user information given to service

    }

}
