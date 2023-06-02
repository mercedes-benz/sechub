// SPDX-License-Identifier: MIT
package com.mercedesbenz.sechub.adapter.pds;

import static com.mercedesbenz.sechub.test.TestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.SocketException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.client.ResourceAccessException;

import com.mercedesbenz.sechub.adapter.AdapterRuntimeContext;
import com.mercedesbenz.sechub.commons.pds.data.PDSJobStatus;
import com.mercedesbenz.sechub.test.FailUntilAmountOfRunsReached;

class PDSContextTest {

    private AdapterRuntimeContext runtimeContext;
    private PDSAdapter adapter;
    private PDSAdapterConfig config;

    private PDSContext contextToTest;

    @BeforeEach
    void beforeEach() {

        adapter = mock(PDSAdapter.class);
        config = mock(PDSAdapterConfig.class);
        runtimeContext = mock(AdapterRuntimeContext.class);

        contextToTest = new PDSContext(config, adapter, runtimeContext);
    }

    @Test
    void context_provides_resilient_consultant_for_socket_exceptions_with_expected_defaults() {
        assertEquals(3, contextToTest.getResilienceConsultant().getMaxRetries());
        assertEquals(10000, contextToTest.getResilienceConsultant().getRetryTimeToWaitInMilliseconds());
    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 42 })
    void context_provides_resilient_runorfail_executor_which_handles_socket_exceptions(int failures) throws Exception {
        /* prepare */
        prepareConsultantToAcceptFailures(failures);

        FailUntilAmountOfRunsReached<SocketException, Void> failUntil = createFailUntil(failures,
                new SocketException("i shall happen only " + failures + " times - so resilience must handle me!"));

        /* execute */
        contextToTest.getResilientRunOrFailExecutor().executeResilient(failUntil);

        /* test */
        assertEquals(failures + 1, failUntil.getTriedRunCount());

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 42 })
    void context_provides_resilient_job_status_result_executor_which_handles_socket_exceptions(int failures) throws Exception {
        prepareConsultantToAcceptFailures(failures);

        PDSJobStatus resultStatusWhenNoFailure = new PDSJobStatus();
        FailUntilAmountOfRunsReached<SocketException, PDSJobStatus> failUntil = createFailUntil(failures,
                new SocketException("i shall happen only " + failures + " times - so resilience must handle me!"), resultStatusWhenNoFailure);

        /* execute */
        PDSJobStatus result = contextToTest.getResilientJobStatusResultExecutor().executeResilient(failUntil);

        /* test */
        assertEquals(failures + 1, failUntil.getTriedRunCount());
        assertEquals(resultStatusWhenNoFailure, result);

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 42 })
    void context_provides_resilient_string_result_executor_which_handles_socket_exceptions(int failures) throws Exception {
        prepareConsultantToAcceptFailures(failures);

        String resultStatusWhenNoFailure = "i am the result";
        FailUntilAmountOfRunsReached<SocketException, String> failUntil = createFailUntil(failures,
                new SocketException("i shall happen only " + failures + " times - so resilience must handle me!"), resultStatusWhenNoFailure);

        /* execute */
        String result = contextToTest.getResilientStringResultExecutor().executeResilient(failUntil);

        /* test */
        assertEquals(failures + 1, failUntil.getTriedRunCount());
        assertEquals(resultStatusWhenNoFailure, result);

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 42 })
    void context_provides_resilient_runorfail_executor_which_handles_resource_access_exceptions(int failures) throws Exception {
        /* prepare */
        prepareConsultantToAcceptFailures(failures);

        FailUntilAmountOfRunsReached<ResourceAccessException, Void> failUntil = createFailUntil(failures,
                new ResourceAccessException("i shall happen only " + failures + " times - so resilience must handle me!"));

        /* execute */
        contextToTest.getResilientRunOrFailExecutor().executeResilient(failUntil);

        /* test */
        assertEquals(failures + 1, failUntil.getTriedRunCount());

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 42 })
    void context_provides_resilient_job_status_result_executor_which_handles_resource_access_exceptions(int failures) throws Exception {
        prepareConsultantToAcceptFailures(failures);

        PDSJobStatus resultStatusWhenNoFailure = new PDSJobStatus();
        FailUntilAmountOfRunsReached<ResourceAccessException, PDSJobStatus> failUntil = createFailUntil(failures,
                new ResourceAccessException("i shall happen only " + failures + " times - so resilience must handle me!"), resultStatusWhenNoFailure);

        /* execute */
        PDSJobStatus result = contextToTest.getResilientJobStatusResultExecutor().executeResilient(failUntil);

        /* test */
        assertEquals(failures + 1, failUntil.getTriedRunCount());
        assertEquals(resultStatusWhenNoFailure, result);

    }

    @ParameterizedTest
    @ValueSource(ints = { 0, 1, 2, 42 })
    void context_provides_resilient_string_result_executor_which_handles_resource_access_exceptions(int failures) throws Exception {
        prepareConsultantToAcceptFailures(failures);

        String resultStatusWhenNoFailure = "i am the result";
        FailUntilAmountOfRunsReached<ResourceAccessException, String> failUntil = createFailUntil(failures,
                new ResourceAccessException("i shall happen only " + failures + " times - so resilience must handle me!"), resultStatusWhenNoFailure);

        /* execute */
        String result = contextToTest.getResilientStringResultExecutor().executeResilient(failUntil);

        /* test */
        assertEquals(failures + 1, failUntil.getTriedRunCount());
        assertEquals(resultStatusWhenNoFailure, result);

    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 5 })
    void context_provides_resilient_runorfail_executor_which_does_not_handle_illegal_state_exceptions(int failures) throws Exception {
        /* prepare */
        prepareConsultantToAcceptFailures(failures);

        FailUntilAmountOfRunsReached<IllegalStateException, Void> failUntil = createFailUntil(failures,
                new IllegalStateException("i am not handled by the consultant - so must fail directly"));

        /* execute */
        assertThrows(IllegalStateException.class, () -> contextToTest.getResilientRunOrFailExecutor().executeResilient(failUntil));

        /* test */
        assertEquals(1, failUntil.getTriedRunCount());

    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 5 })
    void context_provides_resilient_job_status_result_executor_which_does_not_handle_illegal_state_exceptions(int failures) throws Exception {
        prepareConsultantToAcceptFailures(failures);

        PDSJobStatus resultStatusWhenNoFailure = new PDSJobStatus();
        FailUntilAmountOfRunsReached<IllegalStateException, PDSJobStatus> failUntil = createFailUntil(failures,
                new IllegalStateException("i am not handled by the consultant - so must fail directly"), resultStatusWhenNoFailure);

        /* execute */
        assertThrows(IllegalStateException.class, () -> contextToTest.getResilientJobStatusResultExecutor().executeResilient(failUntil));

        /* test */
        assertEquals(1, failUntil.getTriedRunCount());

    }

    @ParameterizedTest
    @ValueSource(ints = { 1, 2, 5 })
    void context_provides_resilient_string_result_executor_which_does_not_handle_illegal_state_exceptions(int failures) throws Exception {
        prepareConsultantToAcceptFailures(failures);

        String resultStatusWhenNoFailure = "i am the result";
        FailUntilAmountOfRunsReached<IllegalStateException, String> failUntil = createFailUntil(failures,
                new IllegalStateException("i am not handled by the consultant - so must fail directly"), resultStatusWhenNoFailure);

        /* execute */
        assertThrows(IllegalStateException.class, () -> contextToTest.getResilientStringResultExecutor().executeResilient(failUntil));

        /* test */
        assertEquals(1, failUntil.getTriedRunCount());

    }

    private void prepareConsultantToAcceptFailures(int acceptedFailures) {
        PDSAdapterResilienceConsultant consultant = contextToTest.getResilienceConsultant();
        consultant.setMaxRetries(acceptedFailures + 1);
        consultant.setRetryTimeToWaitInMilliseconds(1);
    }

}
