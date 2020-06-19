package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class PDSMarkReadyToStartJobServiceTest {

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
