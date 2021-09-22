package com.daimler.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class PDSUpdateJobStreamContentServiceTest {

    private PDSUpdateJobStreamContentService serviceToTest;
    private PDSJobRepository repository;
    private PDSStreamContentUpdateChecker refreshCheckCalculator;
    private PDSJobTransactionService jobTransactionService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new PDSUpdateJobStreamContentService();
        repository=mock(PDSJobRepository.class);
        refreshCheckCalculator = mock(PDSStreamContentUpdateChecker.class);
        jobTransactionService = mock(PDSJobTransactionService.class);
        
        serviceToTest.repository=repository;
        serviceToTest.refreshCheckCalculator=refreshCheckCalculator;
        serviceToTest.jobTransactionService=jobTransactionService;
    }
    
    @Test
    void when_job_does_not_exist_a_not_found_exception_is_thrown() {
        /* prepare */
        UUID notExistingJobUUID = UUID.randomUUID();
        
        /* execute + test */
        assertThrows(IllegalStateException.class, ()-> serviceToTest.setJobStreamAsText(notExistingJobUUID,"output","error"));
        
    }
    
    @Test
    void set_job_stream_as_text__stores_updated_job_via_job_transaction_service() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        when(refreshCheckCalculator.isUpdateRequestedAndNecessary(job)).thenReturn(true);
        
        /* check precondition */
        assertNull(job.getLastStreamTxtUpdate());
        assertNull(job.getOutputStreamText());
        assertNull(job.getErrorStreamText());
        
        /* execute*/
        serviceToTest.setJobStreamAsText(job.getUUID(),"output","error");
        
        /* test */
        ArgumentCaptor<PDSJob> storedJob = ArgumentCaptor.forClass(PDSJob.class);
        verify(jobTransactionService).saveInOwnTransaction(storedJob.capture());
        
        PDSJob value = storedJob.getValue();
        assertEquals("output", value.getOutputStreamText());
        assertEquals("error", value.getErrorStreamText());
        assertNotNull(value.getLastStreamTxtUpdate());
        
        // test update time stamp as expected 
        Duration duration = Duration.between(value.getLastStreamTxtUpdate(), LocalDateTime.now());
        long durationMillis = duration.toMillis();
        assertTrue(durationMillis<300);
      
    }
    
    private PDSJob prepareJobCanBeFound() {
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID=jobUUID;
        
        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));
        return job;
    }

}
