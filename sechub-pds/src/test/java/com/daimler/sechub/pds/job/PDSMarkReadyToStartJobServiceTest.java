package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

public class PDSMarkReadyToStartJobServiceTest {
    @Rule
    public ExpectedException expected = ExpectedException.none();
    
    private PDSMarkReadyToStartJobService serviceToTest;
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
        
        serviceToTest = new PDSMarkReadyToStartJobService();
        serviceToTest.repository=repository;
    }
    
    @Test
    public void mark_ready_to_start_cannot_be_done_when_any_other_state_then_created() {
        for (PDSJobStatusState state: PDSJobStatusState.values()) {
            if (state==PDSJobStatusState.CREATED) {
                continue;
            }
            assertFailsWithIllegalStateFor(state);
        }
    }
    
    private void assertFailsWithIllegalStateFor(PDSJobStatusState state) {
        /* prepare */
        job.setState(state);
        /* test */
        expected.expect(IllegalStateException.class);
        expected.expectMessage("Cannot mark job as ready to start");

        /* execute */
        serviceToTest.markReadyToStart(jobUUID);
    }
    
    @Test
    public void mark_ready_to_start_cannot_be_done_when_job_not_exists() {
        /* check precondition */
        assertEquals(PDSJobStatusState.CREATED, job.state); 
        
        
        /* test */
        expected.expect(IllegalArgumentException.class);
        expected.expectMessage("Given job does not exist");
  
        /* execute */
        UUID notExistingJobUUID = UUID.randomUUID();
        serviceToTest.markReadyToStart(notExistingJobUUID);
        
    }

    @Test
    public void markReadyToStart_changes_state_to_READY_TO_START() {
        /* check precondition */
        assertEquals(PDSJobStatusState.CREATED, job.state); 
        
        /* execute */
        serviceToTest.markReadyToStart(jobUUID);
        
        /* test */
        verify(repository).findById(jobUUID); // check loaded
        
        assertEquals(PDSJobStatusState.READY_TO_START, job.state); // check job changed 
        ArgumentCaptor<PDSJob> jobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(repository).save(jobCaptor.capture()); // check saved changed job
        assertEquals(PDSJobStatusState.READY_TO_START,jobCaptor.getValue().getState());
        
        
    }

}
