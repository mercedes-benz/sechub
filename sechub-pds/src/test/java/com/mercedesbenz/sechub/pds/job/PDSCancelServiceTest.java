package com.mercedesbenz.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService;
import com.mercedesbenz.sechub.pds.execution.PDSExecutionService.CancelResult;

class PDSCancelServiceTest {

    private PDSExecutionService executionService;
    private PDSJobRepository repository;

    private PDSCancelService serviceToTest;

    @BeforeEach
    void beforeEach() {
        repository = mock(PDSJobRepository.class);
        executionService = mock(PDSExecutionService.class);

        serviceToTest = new PDSCancelService();
        serviceToTest.executionService = executionService;
        serviceToTest.repository = repository;

        serviceToTest.minutesToWaitBeforeTreatedAsOrphaned = 0; // define here just 0 will not wait
    }

    @Test
    @DisplayName("One job in state CANCEL_REQUESTED - found - no additional save done")
    void cancelJobsWhereRequested_1() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID = jobUUID;

        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(Arrays.asList(job));
        when(executionService.cancel(jobUUID)).thenReturn(CancelResult.JOB_FOUND_CANCEL_WAS_DONE);

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        verify(executionService).cancel(jobUUID);
        verify(repository, never()).save(job);
    }

    @Test
    @DisplayName("One job in state CANCEL_REQUESTED - found but cancel not possible - treat as orphaned (saved as CANCELED)")
    void cancelJobsWhereRequested_2() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID = jobUUID;
        job.state = PDSJobStatusState.CANCEL_REQUESTED;

        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(Arrays.asList(job));
        when(executionService.cancel(jobUUID)).thenReturn(CancelResult.JOB_FOUND_CANCEL_WAS_NOT_POSSIBLE);

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        ArgumentCaptor<PDSJob> pdsJobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(executionService).cancel(jobUUID);
        verify(repository).save(pdsJobCaptor.capture());
        PDSJob jobSaved = pdsJobCaptor.getValue();
        assertEquals(PDSJobStatusState.CANCELED, jobSaved.getState());

    }

    @Test
    @DisplayName("One job in state CANCEL_REQUESTED - found but job already done")
    void cancelJobsWhereRequested_3() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID = jobUUID;
        job.state = PDSJobStatusState.CANCEL_REQUESTED;

        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(Arrays.asList(job));
        when(executionService.cancel(jobUUID)).thenReturn(CancelResult.JOB_FOUND_JOB_ALREADY_DONE);

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        verify(executionService).cancel(jobUUID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("One job in state CANCEL_REQUESTED - not found - but Job just created so not orphaned")
    void cancelJobsWhereRequested_4() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID = jobUUID;
        job.state = PDSJobStatusState.CANCEL_REQUESTED;
        job.created = LocalDateTime.now();

        serviceToTest.minutesToWaitBeforeTreatedAsOrphaned = 2;

        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(Arrays.asList(job));
        when(executionService.cancel(jobUUID)).thenReturn(CancelResult.JOB_NOT_FOUND);

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        verify(executionService).cancel(jobUUID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("One job in state CANCEL_REQUESTED - found - job not treated as orphaned, even when no too old")
    void cancelJobsWhereRequested_5() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID = jobUUID;
        job.state = PDSJobStatusState.CANCEL_REQUESTED;
        job.created = LocalDateTime.now().minusMinutes(120);

        serviceToTest.minutesToWaitBeforeTreatedAsOrphaned = 2;

        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(Arrays.asList(job));
        when(executionService.cancel(jobUUID)).thenReturn(CancelResult.JOB_FOUND_CANCEL_WAS_DONE);

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        verify(executionService).cancel(jobUUID);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("One job in state CANCEL_REQUESTED - not found - job too old, so treated as orphaned (saved as CANCELED)")
    void cancelJobsWhereRequested_6() {
        /* prepare */
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID = jobUUID;
        job.state = PDSJobStatusState.CANCEL_REQUESTED;
        job.created = LocalDateTime.now().minusMinutes(61);

        serviceToTest.minutesToWaitBeforeTreatedAsOrphaned = 60;

        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(Arrays.asList(job));
        when(executionService.cancel(jobUUID)).thenReturn(CancelResult.JOB_NOT_FOUND);

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        ArgumentCaptor<PDSJob> pdsJobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(executionService).cancel(jobUUID);
        verify(repository).save(pdsJobCaptor.capture());
        PDSJob jobSaved = pdsJobCaptor.getValue();
        assertEquals(PDSJobStatusState.CANCELED, jobSaved.getState());

    }

    @Test
    @DisplayName("No jobs in state CANCEL_REQUESTED")
    void cancelJobsWhereRequested_7() {
        /* prepare */
        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(Arrays.asList());

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        verify(executionService, never()).cancel(any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Four jobs in state CANCEL_REQUESTED - different states, must be handled correct")
    void cancelJobsWhereRequested_8() {
        /* prepare */
        serviceToTest.minutesToWaitBeforeTreatedAsOrphaned = 60;

        List<PDSJob> queryResult = new ArrayList<>();
        PDSJob job1NotFoundInExecutionButOrphaned = new PDSJob();
        job1NotFoundInExecutionButOrphaned.uUID = UUID.randomUUID();
        job1NotFoundInExecutionButOrphaned.created = LocalDateTime.now().minusMinutes(60);
        queryResult.add(job1NotFoundInExecutionButOrphaned);

        PDSJob job2NotFoundInExecutionNotOrphaned = new PDSJob();
        job2NotFoundInExecutionNotOrphaned.uUID = UUID.randomUUID();
        job2NotFoundInExecutionNotOrphaned.created = LocalDateTime.now().minusMinutes(58);
        queryResult.add(job2NotFoundInExecutionNotOrphaned);

        PDSJob job3FoundInExecutionCancelDone = new PDSJob();
        job3FoundInExecutionCancelDone.uUID = UUID.randomUUID();
        job3FoundInExecutionCancelDone.created = LocalDateTime.now().minusMinutes(30);
        queryResult.add(job3FoundInExecutionCancelDone);

        PDSJob job4FoundInExeuctionCancelNotPossible = new PDSJob();
        job4FoundInExeuctionCancelNotPossible.uUID = UUID.randomUUID();
        job4FoundInExeuctionCancelNotPossible.created = LocalDateTime.now().minusMinutes(61);
        queryResult.add(job4FoundInExeuctionCancelNotPossible);

        when(repository.findAllJobsInState(PDSJobStatusState.CANCEL_REQUESTED)).thenReturn(queryResult);
        when(executionService.cancel(job1NotFoundInExecutionButOrphaned.uUID)).thenReturn(CancelResult.JOB_NOT_FOUND);
        when(executionService.cancel(job2NotFoundInExecutionNotOrphaned.uUID)).thenReturn(CancelResult.JOB_NOT_FOUND);

        when(executionService.cancel(job3FoundInExecutionCancelDone.uUID)).thenReturn(CancelResult.JOB_FOUND_CANCEL_WAS_DONE);
        when(executionService.cancel(job4FoundInExeuctionCancelNotPossible.uUID)).thenReturn(CancelResult.JOB_FOUND_CANCEL_WAS_NOT_POSSIBLE);

        /* execute */
        serviceToTest.handleJobCancelRequests();

        /* test */
        ArgumentCaptor<UUID> uuidCaptor = ArgumentCaptor.forClass(UUID.class);

        verify(executionService, times(4)).cancel(uuidCaptor.capture());
        List<UUID> canceledJobUUIDs = uuidCaptor.getAllValues();
        assertTrue(canceledJobUUIDs.contains(job1NotFoundInExecutionButOrphaned.uUID));
        assertTrue(canceledJobUUIDs.contains(job2NotFoundInExecutionNotOrphaned.uUID));
        assertTrue(canceledJobUUIDs.contains(job3FoundInExecutionCancelDone.uUID));
        assertTrue(canceledJobUUIDs.contains(job4FoundInExeuctionCancelNotPossible.uUID));

        ArgumentCaptor<PDSJob> pdsJobCaptor = ArgumentCaptor.forClass(PDSJob.class);
        verify(repository, times(2)).save(pdsJobCaptor.capture());

        List<PDSJob> jobsSaved = pdsJobCaptor.getAllValues();
        assertTrue(jobsSaved.contains(job1NotFoundInExecutionButOrphaned));
        assertTrue(jobsSaved.contains(job4FoundInExeuctionCancelNotPossible));

    }

}
