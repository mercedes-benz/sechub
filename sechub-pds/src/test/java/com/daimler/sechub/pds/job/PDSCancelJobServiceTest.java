package com.daimler.sechub.pds.job;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import com.daimler.sechub.pds.PDSNotAcceptableException;
import com.daimler.sechub.pds.execution.PDSExecutionService;

public class PDSCancelJobServiceTest {

    private PDSCancelJobService serviceToTest;
    private UUID jobUUID;
    private PDSJobRepository repository;
    private PDSJob job;

    private PDSExecutionService executionService;

    @Before
    public void before() throws Exception {
        repository = mock(PDSJobRepository.class);
        executionService = mock(PDSExecutionService.class);

        jobUUID = UUID.randomUUID();
        job = new PDSJob();
        job.uUID = jobUUID;

        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));

        serviceToTest = new PDSCancelJobService();
        serviceToTest.repository = repository;
        serviceToTest.executionService = executionService;
    }

    @Test
    public void canceling_a_job_not_in_state_running_throws_a_not_acceptable_exception() {
        /* prepare */
        for (PDSJobStatusState state : PDSJobStatusState.values()) {
            if (state == PDSJobStatusState.CANCELED || state == PDSJobStatusState.RUNNING) {
                continue;
            }
            assertFailsWithNotAcceptableFor(state);
        }

    }
    
    @Test
    public void canceling_a_running_job_calls_execution_service_cancel_operation() {
        /* prepare */
        job.state = PDSJobStatusState.RUNNING;

        /* execute */
        serviceToTest.cancelJob(jobUUID);

        /* test */
        verify(executionService).cancel(jobUUID);

    }

    @Test
    public void canceling_a_running_job_does_change_job_state_to_canceeled() {
        /* prepare */
        job.state = PDSJobStatusState.RUNNING;

        /* execute */
        serviceToTest.cancelJob(jobUUID);

        /* test */
        assertEquals(PDSJobStatusState.CANCELED, job.getState());

    }
    
    private void assertFailsWithNotAcceptableFor(PDSJobStatusState state) {
        /* prepare */
        job.setState(state);
        try {
            /* execute */
            serviceToTest.cancelJob(jobUUID);
            
        }catch(PDSNotAcceptableException e) {
            assertTrue(e.getMessage().contains("accepted is only:[RUNNING]"));
        }

    }

}
