// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.pds.execution;

import static org.mockito.Mockito.*;

import java.util.concurrent.Future;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatusState;
import com.mercedesbenz.sechub.pds.job.PDSJob;
import com.mercedesbenz.sechub.pds.job.PDSJobRepository;

class PDSApplyFutureExecutionResultToJobServiceTest {

    private PDSApplyFutureExecutionResultToJobService serviceToTest;
    private Future<PDSExecutionResult> future;
    private PDSJob job;
    private PDSJobRepository repository;
    private PDSExecutionResult result;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void beforeEach() throws Exception {
        serviceToTest = new PDSApplyFutureExecutionResultToJobService();
        repository = mock(PDSJobRepository.class);
        serviceToTest.repository = repository;

        future = mock(Future.class);
        job = mock(PDSJob.class);

        result = mock(PDSExecutionResult.class);
        when(future.get()).thenReturn(result);
    }

    @Test
    void future_canceled_then_job_is_set_to_canceled_and_then_stored() throws Exception {

        /* prepare */
        when(future.isCancelled()).thenReturn(true);

        /* execute */
        serviceToTest.applyResultToJob(future, job);

        /* test */
        InOrder inOrder = inOrder(job, repository);
        inOrder.verify(job).setState(PDSJobStatusState.CANCELED);
        inOrder.verify(repository).save(job);

    }

    @Test
    void job_with_result() throws Exception {

        /* prepare */
        when(future.isCancelled()).thenReturn(false);
        when(result.getResult()).thenReturn("result1");
        when(result.isEncryptionFailure()).thenReturn(false);
        when(result.isFailed()).thenReturn(false);
        when(result.isCanceled()).thenReturn(false);

        /* execute */
        serviceToTest.applyResultToJob(future, job);

        /* test */
        InOrder inOrder = inOrder(job, repository);
        inOrder.verify(job).setResult("result1");
        inOrder.verify(job).setState(PDSJobStatusState.DONE);
        inOrder.verify(repository).save(job);

    }

    @Test
    void job_canceled() throws Exception {

        /* prepare */
        when(future.isCancelled()).thenReturn(false);
        when(result.getResult()).thenReturn("");
        when(result.isEncryptionFailure()).thenReturn(false);
        when(result.isFailed()).thenReturn(false);
        when(result.isCanceled()).thenReturn(true);

        /* execute */
        serviceToTest.applyResultToJob(future, job);

        /* test */
        InOrder inOrder = inOrder(job, repository);
        inOrder.verify(job).setResult("");
        inOrder.verify(job).setState(PDSJobStatusState.CANCELED);
        inOrder.verify(repository).save(job);

    }

    @Test
    void job_with_encryption_failure() throws Exception {

        /* prepare */
        when(future.isCancelled()).thenReturn(false);
        when(result.getResult()).thenReturn("");
        when(result.isFailed()).thenReturn(true);
        when(result.isCanceled()).thenReturn(false);
        when(result.isEncryptionFailure()).thenReturn(true);

        /* execute */
        serviceToTest.applyResultToJob(future, job);

        /* test */
        InOrder inOrder = inOrder(job, repository);
        inOrder.verify(job).setResult("");
        inOrder.verify(job).setState(PDSJobStatusState.FAILED);
        inOrder.verify(job).setEncryptionOutOfSync(true);
        inOrder.verify(repository).save(job);

    }

}
