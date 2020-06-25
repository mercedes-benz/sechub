package com.daimler.sechub.pds.execution;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class PDSExecutionServiceTest {

    private PDSExecutionService serviceToTest;

    @Before
    public void before() throws Exception {
        serviceToTest = new PDSExecutionService();
    }

    @Test
    public void when_service_queuemax_is_zero_queue_is_always_full() {
        /* prepare */
        serviceToTest.queueMax=0;
        serviceToTest.postConstruct(); // simulate spring boot container...
        
        /* execute + test */
        assertTrue(serviceToTest.isQueueFull());
        
        
    }

}
