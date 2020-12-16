// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.daimler.sechub.pds.PDSNotFoundException;
import com.daimler.sechub.test.junit4.ExpectedExceptionFactory;

public class PDSGetJobStatusServiceTest {

    @Rule
    public ExpectedException expected = ExpectedExceptionFactory.none();
    
    private PDSGetJobStatusService serviceToTest;
    private UUID jobUUID;
    private PDSJobRepository repository;
    private PDSJob job;


    @Before
    public void before() throws Exception {
        repository = mock(PDSJobRepository.class);
        
        jobUUID=UUID.randomUUID();
        job = new PDSJob();
        job.uUID=jobUUID;
        job.created=LocalDateTime.of(2020, 06, 23,16,35,01);
        job.owner="theOwner";
        
        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));
        
        serviceToTest = new PDSGetJobStatusService();
        serviceToTest.repository=repository;
    }

    @Test
    public void get_status_works_for_any_state() {
        for (PDSJobStatusState state: PDSJobStatusState.values()) {
            String ended = null;
            if (PDSJobStatusState.DONE.equals(state)) {
                ended = "2020-06-23T16:37:03";
                job.ended=LocalDateTime.of(2020, 06, 23,16,37,03);
            }else {
                ended="";
            }
            fetchStateWorksFor(state,ended);
        }
    }
    
    @Test
    public void job_not_found_throws_pds_not_found_exception() {
        /* test */
        expected.expect(PDSNotFoundException.class);
        expected.expectMessage("Given job does not exist");
  
        /* execute */
        UUID notExistingJobUUID = UUID.randomUUID();
        serviceToTest.getJobStatus(notExistingJobUUID);
        
    }
    
    
    private void fetchStateWorksFor(PDSJobStatusState state,String eexpectedEnded) {
        /* prepare */
        job.setState(state);
        /* prepare */
        job.state=PDSJobStatusState.DONE;
        
        /* execute */
        PDSJobStatus result = serviceToTest.getJobStatus(jobUUID);
        
        /* test */
        assertEquals(job.owner, result.owner);
        assertEquals("2020-06-23T16:35:01", result.created);
        assertEquals(eexpectedEnded, result.ended);
        assertEquals(job.state.name(), result.state);
    }

}
