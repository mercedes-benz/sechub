// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.domain.schedule.encryption;

import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;

class ScheduleRefreshEncryptionServiceSetupTriggerServiceTest {

    private ScheduleRefreshEncryptionServiceSetupTriggerService serviceToTest;
    private ScheduleEncryptionService encryptionService;

    @Test
    void triggers_refresh_on_encryption_service() {
        /* prepare */
        encryptionService = mock(ScheduleEncryptionService.class);

        serviceToTest = new ScheduleRefreshEncryptionServiceSetupTriggerService();
        serviceToTest.encryptionService = encryptionService;

        /* execute */
        serviceToTest.triggerEncryptionSetupRefresh();

        /* test */
        verify(encryptionService).refreshEncryptionPoolAndLatestPoolIdIfNecessary();
    }

}
