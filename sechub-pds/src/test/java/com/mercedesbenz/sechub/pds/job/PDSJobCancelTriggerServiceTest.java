// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

import com.mercedesbenz.sechub.test.TestUtil;

class PDSJobCancelTriggerServiceTest {

    private PDSJobCancelTriggerService serviceToTest;
    private PDSCancelService cancelService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new PDSJobCancelTriggerService();

        cancelService = mock(PDSCancelService.class);
        serviceToTest.cancelService = cancelService;
    }

    @Test
    void trigger_does_call_cancelservice() {
        /* execute */
        serviceToTest.triggerHandleCancelRequests();

        /* test */
        verify(cancelService).handleJobCancelRequests();
    }

    @Test
    void service_is_spring_scheduled() {
        assertTrue(TestUtil.hasAtLeastOneMethodWithAnnotation(serviceToTest.getClass(), Scheduled.class), "Not spring scheduled!");
    }

}
