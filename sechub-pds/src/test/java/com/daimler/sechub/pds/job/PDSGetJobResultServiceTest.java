// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.PDSNotFoundException;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class PDSGetJobResultServiceTest {
    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    
    private PDSGetJobResultService serviceToTest;
    private UUID jobUUID;
    private PDSJobRepository repository;
    private PDSJob job;


    @Before
    public void before() throws Exception {
        repository = mock(PDSJobRepository.class);
        
        jobUUID=UUID.randomUUID();
        job = new PDSJob();
        job.uUID=jobUUID;
        
        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));
        
        serviceToTest = new PDSGetJobResultService();
        serviceToTest.repository=repository;
    }

    @Test
    public void get_result_cannot_be_done_when_any_other_state_then_done() {
        for (PDSJobStatusState state: PDSJobStatusState.values()) {
            if (state==PDSJobStatusState.DONE) {
                continue;
            }
            assertFailsWithNotAcceptableFor(state);
        }
    }
    
    @Test
    public void job_not_found_throws_pds_not_found_exception() {
        /* test */
        expected.expect(PDSNotFoundException.class);
        expected.expectMessage("Given job does not exist");
  
        /* execute */
        UUID notExistingJobUUID = UUID.randomUUID();
        serviceToTest.getJobResult(notExistingJobUUID);
        
    }
    
    @Test
    public void job_rdone_esult_found_and_returned() {
        /* prepare */
        job.state=PDSJobStatusState.DONE;
        job.result="the result";
        
        /* execute */
        String result = serviceToTest.getJobResult(jobUUID);
        
        /* test */
        assertEquals("the result", result);
    }
    
    private void assertFailsWithNotAcceptableFor(PDSJobStatusState state) {
        /* prepare */
        job.setState(state);
        try {
            /* execute */
            serviceToTest.getJobResult(jobUUID);
            
        }catch(PDSNotAcceptableException e) {
            assertTrue(e.getMessage().contains("accepted is only:[DONE]"));
        }

    }
}
