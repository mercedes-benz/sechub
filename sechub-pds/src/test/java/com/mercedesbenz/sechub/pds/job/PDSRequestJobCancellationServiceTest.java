// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.EnumSource.Mode;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.PDSNotAcceptableException;

public class PDSRequestJobCancellationServiceTest {

    private PDSRequestJobCancellationService serviceToTest;
    private UUID jobUUID;
    private PDSJobRepository repository;
    private PDSJob job;
    private PDSJobTransactionService transactionService;

    @BeforeEach
    void before() throws Exception {
        repository = mock(PDSJobRepository.class);
        transactionService = mock(PDSJobTransactionService.class);

        jobUUID = UUID.randomUUID();
        job = new PDSJob();
        job.uUID = jobUUID;

        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));

        serviceToTest = new PDSRequestJobCancellationService();
        serviceToTest.repository = repository;
        serviceToTest.transactionService = transactionService;
    }

    @ParameterizedTest
    @EnumSource(mode = Mode.EXCLUDE, names = { "CANCEL_REQUESTED", "RUNNING" })
    void canceling_a_job_in_unaccepted_state_throws_dedicated_exception(PDSJobStatusState state) {
        /* prepare */
        job.setState(state);
        try {
            /* execute */
            serviceToTest.requestJobCancellation(jobUUID);

        } catch (PDSNotAcceptableException e) {
            assertTrue(e.getMessage().contains("accepted is only:[RUNNING]"));
        }
    }

    @ParameterizedTest
    @EnumSource(mode = Mode.INCLUDE, names = { "CANCEL_REQUESTED", "RUNNING" })
    void canceling_a_job_in_accepted_state_throws_no_exception(PDSJobStatusState state) {
        /* prepare */
        job.setState(state);

        /* execute */
        serviceToTest.requestJobCancellation(jobUUID);

    }

    @Test
    void canceling_a_running_job_marks_job_as_cancel_requested_in_own_transaction() {
        /* prepare */
        job.state = PDSJobStatusState.RUNNING;

        /* execute */
        serviceToTest.requestJobCancellation(jobUUID);

        /* test */
        verify(transactionService).markJobAsCancelRequestedInOwnTransaction(job.getUUID());

    }

    @Test
    void canceling_an_already_cancel_requested_job_does_not_mark_job_as_cancel_requested_again() {
        /* prepare */
        job.state = PDSJobStatusState.CANCEL_REQUESTED;

        /* execute */
        serviceToTest.requestJobCancellation(jobUUID);

        /* test */
        verify(transactionService, never()).markJobAsCancelRequestedInOwnTransaction(job.getUUID());

    }

}
