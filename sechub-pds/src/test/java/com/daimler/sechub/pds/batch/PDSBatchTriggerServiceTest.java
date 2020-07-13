package com.daimler.sechub.pds.batch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.pds.execution.PDSExecutionService;
import com.daimler.sechub.pds.job.PDSJob;
import com.daimler.sechub.pds.job.PDSJobRepository;
import com.daimler.sechub.pds.job.PDSJobStatusState;

public class PDSBatchTriggerServiceTest {

    private PDSBatchTriggerService serviceToTest;
    private PDSExecutionService executionService;
    private PDSJobRepository repository;

    @Before
    public void before() throws Exception {
        executionService = mock(PDSExecutionService.class);
        repository = mock(PDSJobRepository.class);

        serviceToTest = new PDSBatchTriggerService();

        serviceToTest.executionService = executionService;
        serviceToTest.repository = repository;
    }

    @Test
    public void a_job_found_for_next_execution_is_changed_to_state_QUEUED() {
        /* prepare */
        PDSJob job = new PDSJob();
        job.setState(PDSJobStatusState.READY_TO_START);

        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));

        /* check precondition */
        assertEquals(PDSJobStatusState.READY_TO_START, job.getState());

        /* execute */
        serviceToTest.triggerExecutionOfNextJob();

        /* test */
        assertEquals(PDSJobStatusState.QUEUED, job.getState());
    }
    
    @Test
    public void a_job_found_for_next_execution_but_scheduling_disabled_job_is_not_changed() {
        /* prepare */
        serviceToTest.schedulingEnabled=false;
        PDSJob job = new PDSJob();
        job.setState(PDSJobStatusState.READY_TO_START);

        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));

        /* check precondition */
        assertEquals(PDSJobStatusState.READY_TO_START, job.getState());

        /* execute */
        serviceToTest.triggerExecutionOfNextJob();

        /* test */
        assertEquals(PDSJobStatusState.READY_TO_START, job.getState());
    }
    
    @Test
    public void a_job_found_for_next_execution_but_scheduling_disabled_executor_service_not_called() {
        /* prepare */
        serviceToTest.schedulingEnabled=false;
        UUID uuid = UUID.randomUUID();
        PDSJob job = mock(PDSJob.class);
        when(job.getUUID()).thenReturn(uuid);
        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));
        
        /* execute */
        serviceToTest.triggerExecutionOfNextJob();
        
        /* test */
        verify(executionService,never()).addToExecutionQueueAsynchron(uuid);
    }
    
    @Test
    public void a_job_found_for_next_execution_executor_service_called() {
        /* prepare */
        UUID uuid = UUID.randomUUID();
        PDSJob job = mock(PDSJob.class);
        when(job.getUUID()).thenReturn(uuid);
        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));
        
        /* execute */
        serviceToTest.triggerExecutionOfNextJob();
        
        /* test */
        verify(executionService).addToExecutionQueueAsynchron(uuid);
    }

}
