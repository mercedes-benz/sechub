// SPDX-License-Identifier: MIT
package com.daimler.sechub.pds.job;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.daimler.sechub.pds.PDSNotFoundException;

class PDSGetJobStreamContentServiceTest {

    private PDSGetJobStreamContentService serviceToTest;
    private PDSJobRepository repository;
    private PDSStreamContentUpdateChecker refreshCheckCalculator;
    private PDSJobTransactionService jobTransactionService;

    @BeforeEach
    void beforeEach() {
        serviceToTest = new PDSGetJobStreamContentService();
        repository = mock(PDSJobRepository.class);
        refreshCheckCalculator = mock(PDSStreamContentUpdateChecker.class);
        jobTransactionService = mock(PDSJobTransactionService.class);

        serviceToTest.repository = repository;
        serviceToTest.refreshCheckCalculator = refreshCheckCalculator;
        serviceToTest.jobTransactionService = jobTransactionService;

        serviceToTest.timeToWaitForNextCheckInMilliseconds = 10; // faster testing...
    }

    @Test
    void initial_default_settings_are_valid() {
        PDSGetJobStreamContentService plainNewInstance = new PDSGetJobStreamContentService();

        assertTrue(plainNewInstance.maximumRefreshCheckRetries > 2);
        assertTrue(plainNewInstance.maximumRefreshRequestRetries > 1);
        assertTrue(plainNewInstance.timeToWaitForNextCheckInMilliseconds >= 500);
    }

    @Test
    void when_job_does_not_exist_getting_job_errror_stream_not_found_exception_is_thrown() {
        /* prepare */
        UUID notExistingJobUUID = UUID.randomUUID();

        /* execute + test */
        assertThrows(PDSNotFoundException.class, () -> serviceToTest.getJobErrorStreamContentAsText(notExistingJobUUID));

    }

    @Test
    void when_job_does_not_exist_getting_job_output_stream_not_found_exception_is_thrown() {
        /* prepare */
        UUID notExistingJobUUID = UUID.randomUUID();

        /* execute + test */
        assertThrows(PDSNotFoundException.class, () -> serviceToTest.getJobOutputStreamContentAsText(notExistingJobUUID));

    }

    @Test
    void checker_update_NOT_necessary__output_fetch_from_db_but_no_other_interactions() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.outputStreamText = "output1";

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(false);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(false); // after mark for refresh the next check accepts the "update"

        /* execute */
        String result = serviceToTest.getJobOutputStreamContentAsText(job.getUUID());

        /* test */
        assertEquals("output1", result);
        verify(refreshCheckCalculator).isUpdateNecessaryWhenRefreshRequestedNow(job); // must be asked
        verify(jobTransactionService, never()).saveInOwnTransaction(job); // no save done by get
        verify(jobTransactionService, never()).markJobStreamDataRefreshRequestedInOwnTransaction(job.getUUID()); // no refresh requested

    }

    @Test
    void checker_update_NOT_necessary__error_fetch_from_db_but_no_other_interactions() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.errorStreamText = "err1";

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(false);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(false); // after mark for refresh the next check accepts the "update"

        /* execute */
        String result = serviceToTest.getJobErrorStreamContentAsText(job.getUUID());

        /* test */
        assertEquals("err1", result);
        verify(refreshCheckCalculator).isUpdateNecessaryWhenRefreshRequestedNow(job); // must be asked
        verify(jobTransactionService, never()).saveInOwnTransaction(job); // no save done by get
        verify(jobTransactionService, never()).markJobStreamDataRefreshRequestedInOwnTransaction(job.getUUID()); // no refresh requested

    }

    @Test
    void checker_update_necessary__output_fetch_from_db_when_last_update_not_too_long() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.outputStreamText = "output1";

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(true);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(false); // after mark for refresh the next check accepts the "update"

        /* execute */
        String result = serviceToTest.getJobOutputStreamContentAsText(job.getUUID());

        /* test */
        assertEquals("output1", result);
        verify(refreshCheckCalculator).isUpdateNecessaryWhenRefreshRequestedNow(job); // must be asked
        verify(jobTransactionService, never()).saveInOwnTransaction(job); // no save done by get
        verify(jobTransactionService).markJobStreamDataRefreshRequestedInOwnTransaction(job.getUUID()); // must be marked

    }

    @Test
    void checker_update_necessary__error_fetch_from_db_when_last_update_not_too_long() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.errorStreamText = "err1";

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(true);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(false); // after mark for refresh the next check accepts the "update" // after
                                                                                         // mark for refresh the next check accepts the "update"

        /* execute */
        String result = serviceToTest.getJobErrorStreamContentAsText(job.getUUID());

        /* test */
        assertEquals("err1", result);
        verify(refreshCheckCalculator).isUpdateNecessaryWhenRefreshRequestedNow(job); // must be asked
        verify(jobTransactionService, never()).saveInOwnTransaction(job); // no save done by get
        verify(jobTransactionService).markJobStreamDataRefreshRequestedInOwnTransaction(job.getUUID()); // must be marked

    }

    @Test
    void checker_update_necessary__fetch_output_wait_as_long_last_update_too_long_then_return_db_entry() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.outputStreamText = "output1";

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(true);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(true).thenReturn(true).thenReturn(false); // at least one wait

        /* execute */
        String result = serviceToTest.getJobOutputStreamContentAsText(job.getUUID());

        /* test */
        assertEquals("output1", result);
        verify(refreshCheckCalculator).isUpdateNecessaryWhenRefreshRequestedNow(job); // must be asked
        verify(jobTransactionService, never()).saveInOwnTransaction(job); // no save done by get
        verify(jobTransactionService).markJobStreamDataRefreshRequestedInOwnTransaction(job.getUUID()); // must be marked

    }

    @Test
    void checker_update_necessary__fetch_error_wait_as_long_last_update_too_long_then_return_db_entry() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.errorStreamText = "err1";

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(true);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(true).thenReturn(true).thenReturn(false); // at least one wait

        /* execute */
        String result = serviceToTest.getJobErrorStreamContentAsText(job.getUUID());

        /* test */
        assertEquals("err1", result);
        verify(refreshCheckCalculator).isUpdateNecessaryWhenRefreshRequestedNow(job); // must be asked
        verify(jobTransactionService, never()).saveInOwnTransaction(job); // no save done by get
        verify(jobTransactionService).markJobStreamDataRefreshRequestedInOwnTransaction(job.getUUID()); // must be marked

    }

    @Test
    void checker_update_necessary__fetch_output_waits_fails_on_timeout() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.errorStreamText = "err1";

        serviceToTest.maximumRefreshCheckRetries = 1; // only one retry...

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(true);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(true).thenReturn(true); // first wait says still update necessary...

        /* execute + test */
        IllegalStateException result = assertThrows(IllegalStateException.class, () -> serviceToTest.getJobOutputStreamContentAsText(job.getUUID()));
        assertTrue(result.getMessage().contains("Timeout!"));

    }

    @Test
    void checker_update_necessary__fetch_output_waits_fails_not_when_job_in_state_where_no_update_necessary() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.errorStreamText = "err1";

        serviceToTest.maximumRefreshCheckRetries = 1; // only one retry...

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(false);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(true);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(true).thenReturn(true); // first wait says still update necessary...

        /* execute + test */
        serviceToTest.getJobOutputStreamContentAsText(job.getUUID());

    }

    @Test
    void checker_update_necessary__fetch_error_waits_fails_on_timeout() {
        /* prepare */
        PDSJob job = prepareJobCanBeFound();
        job.errorStreamText = "err1";

        serviceToTest.maximumRefreshCheckRetries = 1; // only one retry...

        when(refreshCheckCalculator.isJobInStateWhereUpdateNecessary(job)).thenReturn(true);
        when(refreshCheckCalculator.isUpdateNecessaryWhenRefreshRequestedNow(job)).thenReturn(true);
        when(refreshCheckCalculator.isLastUpdateTooOld(any(), any())).thenReturn(true).thenReturn(true); // first wait says still update necessary...

        /* execute + test */
        IllegalStateException result = assertThrows(IllegalStateException.class, () -> serviceToTest.getJobErrorStreamContentAsText(job.getUUID()));
        assertTrue(result.getMessage().contains("Timeout!"));

    }

    private PDSJob prepareJobCanBeFound() {
        UUID jobUUID = UUID.randomUUID();
        PDSJob job = new PDSJob();
        job.uUID = jobUUID;

        when(repository.findById(jobUUID)).thenReturn(Optional.of(job));
        return job;
    }

}
