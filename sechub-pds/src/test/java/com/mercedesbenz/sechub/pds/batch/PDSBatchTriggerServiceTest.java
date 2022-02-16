// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.batch;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.mercedesbenz.sechub.pds.execution.PDSExecutionService;
import com.mercedesbenz.sechub.pds.job.PDSJob;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;
import com.mercedesbenz.sechub.pds.job.PDSJobTransactionService;

public class PDSBatchTriggerServiceTest {

    private PDSBatchTriggerService serviceToTest;
    private PDSExecutionService executionService;
    private PDSJobRepository repository;
    private PDSJobTransactionService jobTransactionService;
    private UUID nextJobUUID;
    private PDSJob job;

    @Before
    public void before() throws Exception {
        nextJobUUID = UUID.randomUUID();
        job = mock(PDSJob.class);
        when(job.getUUID()).thenReturn(nextJobUUID);

        executionService = mock(PDSExecutionService.class);
        repository = mock(PDSJobRepository.class);
        jobTransactionService = mock(PDSJobTransactionService.class);
        when(jobTransactionService.findNextJobToExecuteAndMarkAsQueued()).thenReturn(nextJobUUID);

        serviceToTest = new PDSBatchTriggerService();

        serviceToTest.executionService = executionService;
        serviceToTest.repository = repository;
        serviceToTest.jobTransactionService = jobTransactionService;
    }

    @Test
    public void a_job_found_for_next_execution_executor_service_called() {
        /* prepare */
        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));

        /* execute */
        serviceToTest.triggerExecutionOfNextJob();

        /* test */
        verify(executionService).addToExecutionQueueAsynchron(nextJobUUID);
    }

    @Test
    public void a_job_found_for_next_execution_jobtransaction_service_is_called() {
        /* prepare */
        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));

        /* execute */
        serviceToTest.triggerExecutionOfNextJob();

        /* test */
        verify(jobTransactionService).findNextJobToExecuteAndMarkAsQueued();
    }

    @Test
    public void a_job_found_for_next_execution_but_scheduling_disabled_executor_service_is_NOT_called() {
        /* prepare */
        serviceToTest.schedulingEnabled = false;
        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));

        /* execute */
        serviceToTest.triggerExecutionOfNextJob();

        /* test */
        verify(executionService, never()).addToExecutionQueueAsynchron(any());
    }

    @Test
    public void a_job_found_for_next_execution_but_scheduling_disabled_jobtransaction_service_is_NOT_called() {
        /* prepare */
        serviceToTest.schedulingEnabled = false;
        when(repository.findNextJobToExecute()).thenReturn(Optional.of(job));

        /* execute */
        serviceToTest.triggerExecutionOfNextJob();

        /* test */
        verify(jobTransactionService, never()).findNextJobToExecuteAndMarkAsQueued();
    }

}
