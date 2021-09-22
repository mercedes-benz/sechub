package com.daimler.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.pds.PDSNotFoundException;

class PDSCheckJobStatusServiceTest {

    private PDSCheckJobStatusService serviceToTest;
    private PDSStreamContentUpdateChecker refreshCheckCalculator;
    private PDSJobRepository repository;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new PDSCheckJobStatusService();
        
        refreshCheckCalculator = mock(PDSStreamContentUpdateChecker.class);
        repository=mock(PDSJobRepository.class);
        
        serviceToTest.refreshCheckCalculator=refreshCheckCalculator;
        serviceToTest.repository=repository;

    }

    @Test
    void null_argument_throws_illegal_argument_exception() {
        
        /* execute + test */
        assertThrows(IllegalArgumentException.class, ()-> serviceToTest.isJobStreamUpdateNecessary(null));
    }
    
    @Test
    void when_job_does_not_exist_a_not_found_exception_is_thrown() {
        /* prepare */
        UUID notExistingJobUUID = UUID.randomUUID();
        
        /* execute + test */
        assertThrows(PDSNotFoundException.class, ()-> serviceToTest.isJobStreamUpdateNecessary(notExistingJobUUID));
        
    }
    
    @Test
    void when_check_calculator__says_update_NOT_requested_and_necessary__isJobStreamUpdateNecessary_returns_false() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        when(refreshCheckCalculator.isUpdateRequestedAndNecessary(job)).thenReturn(false);
        
        /* execute*/
        boolean result = serviceToTest.isJobStreamUpdateNecessary(job.getUUID());
        
        /* test */
        assertEquals(false,result);
        
    }
    
    @Test
    void when_check_calculator__says_update_requested_and_necessary__isJobStreamUpdateNecessary_returns_true() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        when(refreshCheckCalculator.isUpdateRequestedAndNecessary(job)).thenReturn(true);
        
        /* execute*/
        boolean result = serviceToTest.isJobStreamUpdateNecessary(job.getUUID());
        
        /* test */
        assertEquals(true,result);
        
        
    }


    private PDSJob prepareJobCanBeFound() {
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID=jobUUID;
        
        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));
        return job;
    }

}
